package com.ritesh.paymentorchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration — controls which frontend origins are permitted to call the API.
 *
 * <p>The allowed origin is driven by the {@code CORS_ALLOWED_ORIGINS} environment variable.
 * In local development, it defaults to {@code http://localhost:5173} (Vite dev server).
 * For production, set this variable to the deployed frontend URL (e.g., Vercel URL).
 *
 * <p>Why not use allowOrigins("*")?
 * Wildcard origins prevent browsers from including credentials (cookies, auth headers)
 * in cross-origin requests. Using explicit origins is more secure and allows
 * credential-based auth to be added in the future.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Split comma-separated origins from env var
        List<String> origins = List.of(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Type", "Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
