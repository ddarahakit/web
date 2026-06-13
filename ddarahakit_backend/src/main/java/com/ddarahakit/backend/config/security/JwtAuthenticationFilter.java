package com.ddarahakit.backend.config.security;

import com.ddarahakit.backend.domain.user.model.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE = "ATOKEN";

    private final JwtProvider jwtProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/user/login") ||
                path.startsWith("/user/signup") ||
                path.startsWith("/user/token/refresh") ||
                path.startsWith("/user/logout") ||
                path.startsWith("/oauth2/authorization/");
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = getTokenFromCookie(request);

        // 토큰이 없으면 인증 없이 진행 (permitAll 엔드포인트 처리)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 유효성 검증 (서명 + 만료 + issuer 통합 검증)
        if (!jwtProvider.validateToken(token)) {
            sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
            return;
        }

        // 액세스 토큰 타입 검증 (리프레시 토큰으로 API 접근 방지)
        if (!jwtProvider.isAccessToken(token)) {
            log.warn("[SECURITY] 잘못된 토큰 타입으로 API 접근 시도");
            sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
            return;
        }

        // 인증 정보 설정
        try {
            setAuthentication(token);
        } catch (Exception e) {
            log.error("[TOKEN] 인증 정보 설정 실패: {}", e.getMessage());
            sendUnauthorizedResponse(response, "토큰 처리 중 오류가 발생했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        User user = User.builder()
                .idx(jwtProvider.extractIdx(token))
                .email(jwtProvider.extractEmail(token))
                .role(jwtProvider.extractRole(token))
                .build();

        AuthUserDetails authUserDetails = AuthUserDetails.of(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUserDetails,
                null,
                authUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                String.format("{\"success\":false,\"code\":20001,\"message\":\"%s\",\"results\":null}", message)
        );
    }
}
