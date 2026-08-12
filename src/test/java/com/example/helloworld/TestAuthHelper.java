package com.example.helloworld;

import com.example.helloworld.domain.entities.UserEntity;
import com.example.helloworld.domain.entities.UserRole;
import com.example.helloworld.infra.repositories.UserRepository;
import com.example.helloworld.infra.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class TestAuthHelper {

    private TestAuthHelper() {
    }

    public static String adminToken(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        return tokenFor(userRepository, passwordEncoder, jwtService, UserRole.ADMIN, "admin@test.com");
    }

    public static String userToken(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        return tokenFor(userRepository, passwordEncoder, jwtService, UserRole.USER, "user@test.com");
    }

    public static String tokenFor(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserRole role,
            String email
    ) {
        var user = new UserEntity("Test User", email, passwordEncoder.encode("senhaSegura123"), role);
        user = userRepository.save(user);
        return jwtService.generateAccessToken(user);
    }
}
