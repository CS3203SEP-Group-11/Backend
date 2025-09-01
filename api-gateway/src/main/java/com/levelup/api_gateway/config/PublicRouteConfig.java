package com.levelup.api_gateway.config;

import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;

@Getter
@Component
public class PublicRouteConfig {

    private final Map<String, List<HttpMethod>> publicRoutes = Map.of(
//            "**", List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS) // for testing, allow all
            "/api/auth/**", List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS),
            "/api/courses", List.of(HttpMethod.GET, HttpMethod.OPTIONS),
            "/api/instructors/**", List.of(HttpMethod.GET, HttpMethod.OPTIONS)
    );

    private final AntPathMatcher matcher = new AntPathMatcher();

    public boolean isPublic(String path, HttpMethod method) {
        if (method == HttpMethod.OPTIONS) return true; // allow all preflight
        return publicRoutes.entrySet().stream().anyMatch(e ->
                matcher.match(e.getKey(), path) && e.getValue().contains(method)
        );
    }

}
