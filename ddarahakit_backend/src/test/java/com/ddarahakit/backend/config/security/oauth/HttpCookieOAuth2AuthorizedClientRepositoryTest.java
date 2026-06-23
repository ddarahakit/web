package com.ddarahakit.backend.config.security.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OAuth2 인가요청 쿠키의 JSON(Jackson) 직렬화 ↔ 역직렬화 라운드트립 검증.
 * Java 네이티브 직렬화를 JSON 으로 교체한 변경이 실제 로그인 흐름을 깨지 않는지 보장한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class HttpCookieOAuth2AuthorizedClientRepositoryTest {

    @Autowired
    HttpCookieOAuth2AuthorizedClientRepository repository;

    @Test
    @DisplayName("인가요청을 쿠키로 저장 후 로드하면 동일 값으로 복원된다(JSON 라운드트립)")
    void save_then_load_roundtrip() {
        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("https://api.ddarahakit.com/login/oauth2/code/google")
                .scopes(Set.of("profile", "email"))
                .state("test-state-value")
                .attributes(Map.of("registration_id", "google"))
                .build();

        // 저장 → 응답 쿠키 생성
        MockHttpServletResponse response = new MockHttpServletResponse();
        repository.saveAuthorizationRequest(authRequest, new MockHttpServletRequest(), response);
        assertNotNull(response.getCookie(HttpCookieOAuth2AuthorizedClientRepository.OAUTH2_COOKIE_NAME));

        // 저장된 쿠키를 다음 요청에 실어 로드
        MockHttpServletRequest loadRequest = new MockHttpServletRequest();
        loadRequest.setCookies(response.getCookies());

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(loadRequest);

        assertNotNull(loaded);
        assertEquals(authRequest.getClientId(), loaded.getClientId());
        assertEquals(authRequest.getAuthorizationUri(), loaded.getAuthorizationUri());
        assertEquals(authRequest.getRedirectUri(), loaded.getRedirectUri());
        assertEquals(authRequest.getState(), loaded.getState());
        assertEquals(authRequest.getScopes(), loaded.getScopes());
        assertEquals("google", loaded.getAttributes().get("registration_id"));
    }

    @Test
    @DisplayName("손상된 쿠키 값은 예외 없이 null 로 처리된다")
    void corrupted_cookie_returns_null() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie(
                HttpCookieOAuth2AuthorizedClientRepository.OAUTH2_COOKIE_NAME, "not-a-valid-encrypted-value"));

        assertNull(repository.loadAuthorizationRequest(request));
    }
}
