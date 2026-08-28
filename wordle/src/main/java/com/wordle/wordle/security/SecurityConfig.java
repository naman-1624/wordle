package com.wordle.wordle.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final String corsOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          @Value("${wordle.cors.origins}") String corsOrigins) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsOrigins = corsOrigins;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Frontend routes (controllers forward to the static files below)
                        .requestMatchers("/", "/game", "/login", "/hints", "/statistics", "/loss",
                                "/leaderboard", "/help", "/contact", "/privacy-policy",
                                "/terms-of-sale", "/terms-of-service", "/admin").permitAll()
                        // Static frontend assets (served at the root of classpath:/static)
                        .requestMatchers("/index.html", "/landing.html", "/login.html", "/hints.html",
                                "/statistics.html", "/loss.html", "/leaderboard.html", "/admin.html",
                                "/help.html", "/contact.html", "/privacy-policy.html",
                                "/terms-of-sale.html", "/terms-of-service.html",
                                "/style.css", "/script.js", "/favicon.ico").permitAll()
                        .requestMatchers("/**/*.html", "/**/*.css", "/**/*.js", "/**/*.png",
                                "/**/*.jpg", "/**/*.svg", "/**/*.ico", "/**/*.woff", "/**/*.woff2",
                                "/**/*.webmanifest", "/**/*.json").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Login required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Admin access required\"}");
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        if (corsOrigins != null && !corsOrigins.isBlank()) {
            config.setAllowedOrigins(Arrays.stream(corsOrigins.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList());
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
        } else {
            config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}