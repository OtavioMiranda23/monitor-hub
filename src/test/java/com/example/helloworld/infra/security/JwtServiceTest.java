package com.example.helloworld.infra.security;

import com.example.helloworld.domain.entities.UserEntity;
import com.example.helloworld.domain.entities.UserRole;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "monitor-hub-unit-test-secret-key-para-assinatura-hs256",
            900_000,
            604_800_000
    );

    @Test
    void shouldGenerateAccessTokenWithUserSubject() {
        UserEntity user = user("joao@example.com", UserRole.USER);

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractSubject(token)).isEqualTo(user.getId().toString());
        assertThat(jwtService.isAccessToken(token)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void shouldGenerateRefreshTokenAndIdentifyItAsRefresh() {
        UserEntity user = user("joao@example.com", UserRole.USER);

        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.extractSubject(token)).isEqualTo(user.getId().toString());
        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.isAccessToken(token)).isFalse();
    }

    @Test
    void shouldValidateTokenOnlyForItsOwner() {
        UserEntity owner = user("joao@example.com", UserRole.USER);
        UserEntity other = user("maria@example.com", UserRole.ADMIN);

        String token = jwtService.generateAccessToken(owner);

        assertThat(jwtService.isTokenValid(token, owner)).isTrue();
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void shouldRejectTamperedToken() {
        UserEntity user = user("joao@example.com", UserRole.USER);

        String token = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.extractSubject(token + "tampered"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectExpiredToken() {
        UserEntity user = user("joao@example.com", UserRole.USER);
        JwtService expiredJwtService = new JwtService(
                "monitor-hub-unit-test-secret-key-para-assinatura-hs256",
                -1,
                -1
        );

        String token = expiredJwtService.generateAccessToken(user);

        assertThatThrownBy(() -> expiredJwtService.extractSubject(token))
                .isInstanceOf(JwtException.class);
    }

    private UserEntity user(String email, UserRole role) {
        var user = new UserEntity("Test User", email, "hash", role);
        user.setId(UUID.randomUUID());
        return user;
    }
}
