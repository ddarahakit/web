package com.ddarahakit.backend.config;

import com.ddarahakit.backend.config.security.JwtAuthenticationEntryPoint;
import com.ddarahakit.backend.config.security.JwtAuthenticationFilter;
import com.ddarahakit.backend.config.security.oauth.HttpCookieOAuth2AuthorizedClientRepository;
import com.ddarahakit.backend.config.security.oauth.OAuth2AuthenticationFailureHandler;
import com.ddarahakit.backend.config.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.ddarahakit.backend.domain.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${app.domain.server}")
    private String domain;
    // CORS 허용 오리진(쉼표 구분). 미설정 시 app.domain.server 단일 값으로 폴백(dev 호환).
    @Value("${app.domain.allowed-origins:${app.domain.server}}")
    private String allowedOrigins;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizedClientRepository httpCookieOAuth2AuthorizedClientRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of(allowedOrigins.split("\\s*,\\s*")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 적용
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.cors(cors ->
                cors.configurationSource(corsConfigurationSource()));


        http.oauth2Login((config) -> {
            config.authorizationEndpoint(authorization -> authorization
                    .authorizationRequestRepository(httpCookieOAuth2AuthorizedClientRepository)
            );
            config.successHandler(oAuth2AuthenticationSuccessHandler);
            config.failureHandler(oAuth2AuthenticationFailureHandler);
            config.userInfoEndpoint((endpoint) -> endpoint.userService(oAuth2UserService));
        });

        // 기본 정책은 "인증 필요"(화이트리스트). 공개 엔드포인트만 명시적으로 permitAll 하고,
        // 목록에 없는(또는 신규로 추가되는) 엔드포인트는 자동으로 인증이 요구된다.
        http.authorizeHttpRequests((auth) ->
                auth
                        // CORS 프리플라이트는 항상 허용
                        .requestMatchers(OPTIONS, "/**").permitAll()

                        // Swagger / 에러 디스패치 / OAuth2 로그인 플로우 (공개)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // === 인증 필요 엔드포인트 (구체 규칙이 먼저 매칭되도록 아래 공개 규칙보다 위에 둔다) ===
                        .requestMatchers(POST, "/roadmap").authenticated()
                        .requestMatchers(PUT, "/roadmap/*").authenticated()
                        .requestMatchers(DELETE, "/roadmap/*").authenticated()
                        .requestMatchers(POST, "/orders/create", "/orders/verify").authenticated()
                        .requestMatchers(GET, "/orders/check/*").authenticated()
                        .requestMatchers(POST, "/orders/*/free-complete").authenticated()
                        .requestMatchers(POST, "/orders/*/refund").authenticated()
                        .requestMatchers(GET, "/orders/*/receipt").authenticated()
                        .requestMatchers(DELETE, "/orders/*").authenticated()
                        .requestMatchers(POST, "/review/*").authenticated()
                        .requestMatchers(PUT, "/review/*").authenticated()
                        .requestMatchers(DELETE, "/review/*").authenticated()
                        .requestMatchers(GET, "/cart", "/cart/count").authenticated()
                        .requestMatchers(POST, "/cart").authenticated()
                        .requestMatchers(DELETE, "/cart", "/cart/*").authenticated()
                        .requestMatchers(POST, "/community/post", "/community/comment", "/community/upload").authenticated()
                        .requestMatchers(POST, "/community/comment/*/accept").authenticated()
                        .requestMatchers(PUT, "/community/post/*", "/community/comment/*").authenticated()
                        .requestMatchers(DELETE, "/community/post/*", "/community/comment/*").authenticated()
                        .requestMatchers(GET, "/community/scrap").authenticated()
                        .requestMatchers(POST, "/community/scrap/*", "/community/scrap/*/toggle").authenticated()
                        .requestMatchers(DELETE, "/community/scrap/*").authenticated()
                        .requestMatchers(POST, "/lecture/create").authenticated()
                        .requestMatchers(POST, "/course/lecture/complete").authenticated()
                        .requestMatchers(POST, "/user/logout/all").authenticated()
                        .requestMatchers(PUT, "/user/password/update").authenticated()
                        .requestMatchers(GET, "/user/profile", "/user/myreview", "/user/ordered", "/user/myquestion", "/user/mypost", "/user/payments", "/user/study/weekly").authenticated()
                        .requestMatchers(PUT, "/user/profile").authenticated()
                        .requestMatchers(POST, "/user/profile").authenticated()

                        // === 공개(비로그인 허용) 엔드포인트 ===
                        .requestMatchers(GET, "/course/**").permitAll()
                        .requestMatchers(GET, "/roadmap", "/roadmap/*").permitAll()
                        .requestMatchers(GET, "/stats/**").permitAll()
                        .requestMatchers(GET, "/review/*").permitAll()
                        .requestMatchers(GET, "/community/**").permitAll()
                        .requestMatchers(POST, "/user/login", "/user/social/login", "/user/signup", "/user/email/verify", "/user/password/reset").permitAll()
                        // 로그아웃은 만료/무효 토큰 상태에서도 성공해야 하므로 permitAll. 상태변경이라 GET→POST (CSRF 방어).
                        .requestMatchers(POST, "/user/logout").permitAll()
                        .requestMatchers(PUT, "/user/password/reset").permitAll()
                        .requestMatchers(GET, "/user/token/refresh", "/user/email/duplicate", "/user/check", "/user/uuid/check").permitAll()

                        // === 그 외 전부 인증 필요 (화이트리스트 기본 정책) ===
                        .anyRequest().authenticated()
        );

        // 인증 실패(미인증/유효하지 않은 토큰으로 보호 엔드포인트 접근) 시 401(code 20001) 반환.
        // 공개 엔드포인트는 여기까지 오지 않으므로 영향 없다.
        http.exceptionHandling(handler ->
                handler.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
