package com.payflow.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controller methods
public class SecurityConfig {

    // Public paths — no JWT required
    private static final String[] PUBLIC_PATHS = {
        "/api/v1/payments/**",
        "/api/v1/payments/health",
        // Swagger / OpenAPI UI
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/api-docs",
        "/api-docs/**",
        "/api-docs.yaml",
        // Actuator health
        "/actuator/health",
        "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                // FIX #8: All other endpoints now require authentication
                .anyRequest().authenticated()
            )
            // JWT resource server — validates Bearer tokens
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {}) // issuer-uri configured in application.yml
            );

        return http.build();
    }
}
