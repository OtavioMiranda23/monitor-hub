package com.example.helloworld.service;

import com.example.helloworld.controller.dto.CreateUserRequest;
import com.example.helloworld.controller.dto.UpdateUserRequest;
import com.example.helloworld.controller.dto.UserResponse;
import com.example.helloworld.domain.entities.UserEntity;
import com.example.helloworld.infra.exception.UserAlreadyExistsException;
import com.example.helloworld.infra.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Já existe um usuário cadastrado com este e-mail");
        }

        var user = new UserEntity(
                request.name(),
                request.email().toLowerCase(),
                passwordEncoder.encode(request.password()),
                request.role()
        );
        return UserResponse.fromEntity(userRepository.save(user));
    }

    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null) {
            if (!request.email().equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(request.email())) {
                throw new UserAlreadyExistsException("Já existe um usuário cadastrado com este e-mail");
            }
            user.setEmail(request.email().toLowerCase());
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        userRepository.delete(user);
    }
}
