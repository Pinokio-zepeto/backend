package com.example.pinokkio.config;

import com.example.pinokkio.config.jwt.JwtAuthenticationFilter;
import com.example.pinokkio.config.jwt.LogoutService;
import com.example.pinokkio.handler.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final LogoutService logoutService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final String[] SWAGGER_URL = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/api-docs/**"
    };

    private final String[] GET_PERMIT_API_URL = {
            "/",
            "/api/refresh",
            "/api/customer/**", // 추후 수정
            "/api/face-recognition-events",
            "/api/pos/duplicate/**",
            "/api/teller/duplicate/**",
            "/ws/**"
    };

    private final String[] POST_PERMIT_API_URL = {
            "/api/register/pos",
            "/api/register/teller",
            "/api/login/**",
            "/api/mail/send/**",
            "/api/mail/check-auth",
            "/api/refresh",
            "/api/users/auth/token/",
            "/api/customer/**", // 추후 수정
            "/ws/**"
    };

    private final String[] POS_API_URL = {
            "/api/pos/**"
    };

    private final String[] TELLER_API_URL = {
            "/api/tellers/**",
            "/api/meeting/teller"
    };

    private final String[] KIOSK_API_URL = {
            "/api/kiosk/**",
            "/api/meeting/kiosk",
            "/api/orders"
    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(c -> c.disable())
                .formLogin(c -> c.disable())
                .httpBasic(c -> c.disable())
                .headers(c -> c.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeRequests(c -> c.requestMatchers(SWAGGER_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, GET_PERMIT_API_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, POST_PERMIT_API_URL).permitAll()
                        .requestMatchers(KIOSK_API_URL).hasRole("KIOSK")
                        .requestMatchers(POS_API_URL).hasAnyRole("POS", "KIOSK")
                        .requestMatchers(TELLER_API_URL).hasRole("TELLER")
                        .anyRequest().authenticated()
                )

                .logout(c -> c.logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()))

                .exceptionHandling(c -> c.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setExposedHeaders(List.of("*"));

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}