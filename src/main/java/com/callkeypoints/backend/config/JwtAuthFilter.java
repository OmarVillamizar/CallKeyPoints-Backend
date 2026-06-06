package com.callkeypoints.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Validates the bearer token and sets the authenticated principal to the user id taken from
 * the configured claim ({@code app.auth.principal-claim}, default {@code sub}). The claim value
 * must be a UUID, since every record is scoped by a {@code uuid user_id} column.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final String principalClaim;

    public JwtAuthFilter(JwtDecoder jwtDecoder,
                         @Value("${app.auth.principal-claim:sub}") String principalClaim) {
        this.jwtDecoder = jwtDecoder;
        this.principalClaim = principalClaim;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(authHeader.substring(7));
            UUID userId = UUID.fromString(jwt.getClaimAsString(principalClaim));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
