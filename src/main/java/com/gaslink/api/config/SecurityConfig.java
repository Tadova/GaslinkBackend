package com.gaslink.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC ENDPOINTS =====
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/error",
                                "/ws/**" // WebSocket endpoints
                        ).permitAll()

                        // ===== PUBLIC VENDOR & PRODUCT ENDPOINTS =====
                        .requestMatchers(
                                "/api/v1/vendors/nearby",
                                "/api/v1/vendors/{id}",
                                "/api/v1/vendors/{id}/products",
                                "/api/v1/vendors/{id}/gas",
                                "/api/v1/products/active/{vendorId}",
                                "/api/v1/products/gas/{vendorId}",
                                "/api/v1/products/{id}",
                                "/api/v1/orders/active/{vendorId}",
                                "/api/v1/orders/check-vendor/{vendorId}"
                        ).permitAll()

                        // ===== CUSTOMER ENDPOINTS =====
                        .requestMatchers(
                                "/api/v1/orders",
                                "/api/v1/orders/gas",
                                "/api/v1/orders/customer",
                                "/api/v1/orders/{id}/cancel",
                                "/api/v1/orders/{id}/bids",
                                "/api/v1/orders/{orderId}/bids/{bidId}/approve",
                                "/api/v1/users/me",
                                "/api/v1/users/me/**"
                        ).hasRole("CUSTOMER")

                        // ===== VENDOR ENDPOINTS =====
                        .requestMatchers(
                                "/api/v1/orders/nearby",
                                "/api/v1/orders/nearby/regular",
                                "/api/v1/orders/{id}/bid",
                                "/api/v1/orders/{id}/accept",
                                "/api/v1/orders/{id}/reject",
                                "/api/v1/orders/vendor",
                                "/api/v1/orders/statistics/vendor",
                                "/api/v1/orders/{id}/status",
                                "/api/v1/vendors/me/**",
                                "/api/v1/vendors/register",
                                "/api/v1/vendors/toggle-open",
                                "/api/v1/products/**",
                                "/api/v1/inventory/**",
                                "/api/v1/subscriptions/me",
                                "/api/v1/subscriptions/initiate-payment",
                                "/api/v1/subscriptions/confirm-payment",
                                "/api/v1/users/me",
                                "/api/v1/users/me/**"
                        ).hasRole("VENDOR")

                        // ===== MESSAGES (Vendor & Customer) =====
                        .requestMatchers(
                                "/api/v1/messages/**",
                                "/api/v1/messages/order/**",
                                "/api/v1/messages/read/**",
                                "/api/v1/messages/unread/**",
                                "/api/v1/messages/share-location",
                                "/api/v1/messages/share-image"
                        ).hasAnyRole("CUSTOMER", "VENDOR")

                        // ===== CALLS (Vendor & Customer) =====
                        .requestMatchers(
                                "/api/v1/calls/**",
                                "/api/v1/calls/token",
                                "/api/v1/calls/initiate",
                                "/api/v1/calls/join",
                                "/api/v1/calls/end",
                                "/api/v1/calls/reject",
                                "/api/v1/calls/available/**"
                        ).hasAnyRole("CUSTOMER", "VENDOR")

                        // ===== NOTIFICATIONS (All Authenticated Users) =====
                        .requestMatchers(
                                "/api/v1/notifications/**",
                                "/api/v1/notifications/read/**",
                                "/api/v1/notifications/unread/**",
                                "/api/v1/notifications/count"
                        ).hasAnyRole("CUSTOMER", "VENDOR", "ADMIN", "SUPER_ADMIN")

                        // ===== ADMIN ENDPOINTS =====
                        .requestMatchers(
                                "/api/v1/admin/**",
                                "/api/v1/admin/vendors/**",
                                "/api/v1/admin/vendors/{id}/verify",
                                "/api/v1/admin/vendors/{id}/suspend",
                                "/api/v1/admin/vendors/{id}/unsuspend",
                                "/api/v1/admin/vendors/statistics",
                                "/api/v1/subscriptions/all"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // ===== SUPER ADMIN ENDPOINTS =====
                        .requestMatchers(
                                "/api/v1/super-admin/**",
                                "/api/v1/super-admin/admins/**",
                                "/api/v1/super-admin/dashboard",
                                "/api/v1/super-admin/analytics/**",
                                "/api/v1/super-admin/vendors/**",
                                "/api/v1/super-admin/users/**",
                                "/api/v1/super-admin/logs"
                        ).hasRole("SUPER_ADMIN")

                        // ===== ADMIN NOTIFICATIONS (Send to all users) =====
                        .requestMatchers(
                                "/api/v1/notifications/admin/**"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // ===== ALL OTHER ENDPOINTS =====
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://gaslink.com",
                "https://*.gaslink.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}