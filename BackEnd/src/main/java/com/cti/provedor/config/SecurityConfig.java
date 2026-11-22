package com.cti.provedor.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public CorsConfigurationSource corsConfigurationSource(AppProperties properties) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(properties.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", config); return source;
    }
    @Bean public SecurityFilterChain filtro(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http.cors(cors -> {}).csrf(csrf -> csrf.disable())
            .formLogin(login -> login.disable()).httpBasic(basic -> basic.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req,res,err) -> { res.setStatus(401); res.setContentType("application/json;charset=UTF-8"); res.getWriter().write("{\"code\":\"NAO_AUTENTICADO\",\"message\":\"Autenticacao necessaria\"}"); })
                .accessDeniedHandler((req,res,err) -> { res.setStatus(403); res.setContentType("application/json;charset=UTF-8"); res.getWriter().write("{\"code\":\"ACESSO_NEGADO\",\"message\":\"Acesso negado\"}"); }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/cadastro", "/auth/login", "/api/auth/register", "/api/auth/cadastro", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/login-local").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login-local").permitAll()
                .requestMatchers(HttpMethod.GET, "/publico", "/api/publico", "/health", "/api/health").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/clientes/**", "/api/clientes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/consultores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/consultores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/consultores/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
    }
    // O filtro deve executar apenas na cadeia do Spring Security.
    @Bean public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> jwtRegistration(JwtAuthenticationFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false); return registration;
    }
}
