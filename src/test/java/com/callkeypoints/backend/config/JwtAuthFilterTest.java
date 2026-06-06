package com.callkeypoints.backend.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtDecoder, "sub");

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsUuidPrincipal_fromConfiguredClaim() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("tok").header("alg", "none").subject(userId.toString()).build();
        when(jwtDecoder.decode("tok")).thenReturn(jwt);

        runFilter("Bearer tok");

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userId);
    }

    @Test
    void noAuth_whenTokenInvalid() throws Exception {
        when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("bad"));

        runFilter("Bearer tok");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noAuth_whenClaimNotUuid() throws Exception {
        Jwt jwt = Jwt.withTokenValue("tok").header("alg", "none").subject("not-a-uuid").build();
        when(jwtDecoder.decode("tok")).thenReturn(jwt);

        runFilter("Bearer tok");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noAuth_whenHeaderMissing() throws Exception {
        runFilter(null);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private void runFilter(String authHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authHeader != null) {
            request.addHeader("Authorization", authHeader);
        }
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }
}
