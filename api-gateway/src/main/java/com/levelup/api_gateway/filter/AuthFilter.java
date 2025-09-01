package com.levelup.api_gateway.filter;

import com.levelup.api_gateway.config.PublicRouteConfig;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AuthFilter implements WebFilter, Ordered {

    private final String jwtSecret;
    private final PublicRouteConfig publicRouteConfig;

    public AuthFilter(@Value("${jwt.secret}") String jwtSecret, PublicRouteConfig publicRouteConfig) {
        this.jwtSecret = jwtSecret;
        this.publicRouteConfig = publicRouteConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        var path = request.getPath().pathWithinApplication().value();
        var method = request.getMethod();

        // Check if the path is public
        if (publicRouteConfig.isPublic(path, method)) {
            return chain.filter(exchange);
        }

        // For all other paths, JWT cookie is required
        HttpCookie jwtCookie = request.getCookies().getFirst("jwt");
        if (jwtCookie == null) {
            log.warn("Missing JWT cookie for path: {}", path);
            return unauthorized(exchange, "Missing authentication token");
        }

        try {
            String token = jwtCookie.getValue();
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify JWT signature
            JWSVerifier verifier = new MACVerifier(jwtSecret);
            if (!signedJWT.verify(verifier)) {
                log.warn("Invalid JWT signature for path: {}", path);
                return unauthorized(exchange, "Invalid authentication token");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Check token expiration
            Date exp = claims.getExpirationTime();
            if (exp != null && exp.before(new Date())) {
                log.warn("JWT expired for path: {}", path);
                return unauthorized(exchange, "Authentication token expired");
            }

            String userId = claims.getSubject();
            String email = claims.getStringClaim("email");
            String role = claims.getStringClaim("role");

            if (userId == null || role == null || email == null) {
                log.warn("Missing required claims in JWT for path: {}", path);
                return unauthorized(exchange, "Invalid authentication token");
            }

            var mutatedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Email");
                        headers.remove("X-User-Role");
                        headers.add("X-User-Id", userId);
                        headers.add("X-User-Email", email);
                        headers.add("X-User-Role", role);
                    })
                    .build();

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);

            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        } catch (Exception e) {
            log.error("JWT processing error for path {}: {}", path, e.getMessage());
            return unauthorized(exchange, "Invalid authentication token");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"error\":\"unauthorized\",\"message\":\"%s\"}", message);
        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}