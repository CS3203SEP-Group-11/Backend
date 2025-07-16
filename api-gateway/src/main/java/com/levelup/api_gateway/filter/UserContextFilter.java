package com.levelup.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserContextFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    if (securityContext.getAuthentication() instanceof JwtAuthenticationToken jwt) {
                        String userId = jwt.getToken().getSubject();
                        String userEmail = jwt.getToken().getClaimAsString("email");
                        String userRole = jwt.getToken().getClaimAsString("role");

                        var mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", userId)
                                .header("X-User-Email", userEmail)
                                .header("X-User-Role", userRole)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}