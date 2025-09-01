package com.levelup.api_gateway.filter;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UserContextFilter implements GlobalFilter {

    private static final String JWT_COOKIE_NAME = "jwt";
    private static final String SECRET = "your-256-bit-secret"; // For signature validation (optional)

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpCookie jwtCookie = exchange.getRequest().getCookies().getFirst(JWT_COOKIE_NAME);

        if (jwtCookie != null) {
            try {
                String token = jwtCookie.getValue();

                SignedJWT signedJWT = SignedJWT.parse(token);

                // Optional signature verification
                // JWSVerifier verifier = new MACVerifier(SECRET);
                // if (!signedJWT.verify(verifier)) {
                //     log.warn("Invalid JWT signature");
                //     return chain.filter(exchange);
                // }

                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

                String userId = claims.getSubject();
                String email = claims.getStringClaim("email");
                String role = claims.getStringClaim("role");

                var mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                log.error("JWT parsing error: {}", e.getMessage());
            }
        }

        return chain.filter(exchange);
    }
}