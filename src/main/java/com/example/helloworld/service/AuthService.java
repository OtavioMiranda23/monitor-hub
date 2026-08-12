package com.example.helloworld.service;

import com.example.helloworld.controller.dto.AuthRequest;
import com.example.helloworld.controller.dto.AuthResponse;
import com.example.helloworld.domain.entities.UserEntity;
import com.example.helloworld.infra.repositories.UserRepository;
import com.example.helloworld.infra.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse login(AuthRequest request) {
        UserEntity user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }
        if (!user.getActive()) {
            throw new BadCredentialsException("Usuário inativo");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token inválido");
        }

        UserEntity user = userRepository.findById(UUID.fromString(jwtService.extractSubject(refreshToken)))
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

        if (!user.getActive()) {
            throw new BadCredentialsException("Usuário inativo");
        }
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Refresh token inválido");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtService.getAccessExpirationSeconds()
        );
    }
}
