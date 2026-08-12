package com.example.helloworld.controller;

import com.example.helloworld.domain.entities.UserRole;
import com.example.helloworld.infra.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserStoringOnlyBcryptHashAndNotExposingPassword() throws Exception {
        String responseBody = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "João Silva",
                                    "email": "joao@example.com",
                                    "password": "senhaSegura123",
                                    "role": "USER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        UUID userId = UUID.fromString(json.get("id").asText());

        var user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("senhaSegura123");
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("senhaSegura123", user.getPassword())).isTrue();
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "João Silva",
                                    "email": "joao@example.com",
                                    "password": "senhaSegura123",
                                    "role": "USER"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "João Souza",
                                    "email": "joao@example.com",
                                    "password": "outraSenha123",
                                    "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito"));
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "email-invalido",
                                    "password": "123",
                                    "role": "USER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldFindUserById() throws Exception {
        UUID userId = createUser("Maria", "maria@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAllUsers() throws Exception {
        createUser("Maria", "maria@example.com", UserRole.ADMIN);
        createUser("João", "joao@example.com", UserRole.USER);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateUserNameEmailAndPassword() throws Exception {
        UUID userId = createUser("Maria", "maria@example.com", UserRole.ADMIN);

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Maria Souza",
                                    "email": "maria.souza@example.com",
                                    "password": "novaSenhaSegura1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Souza"))
                .andExpect(jsonPath("$.email").value("maria.souza@example.com"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        var user = userRepository.findById(userId).orElseThrow();
        assertThat(passwordEncoder.matches("novaSenhaSegura1", user.getPassword())).isTrue();
    }

    @Test
    void shouldRejectUpdateWithEmailAlreadyInUse() throws Exception {
        createUser("Maria", "maria@example.com", UserRole.ADMIN);
        UUID joaoId = createUser("João", "joao@example.com", UserRole.USER);

        mockMvc.perform(put("/users/{id}", joaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "maria@example.com"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        UUID userId = createUser("Maria", "maria@example.com", UserRole.ADMIN);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(userId)).isFalse();
    }

    private UUID createUser(String name, String email, UserRole role) throws Exception {
        String responseBody = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "email": "%s",
                                    "password": "senhaSegura123",
                                    "role": "%s"
                                }
                                """.formatted(name, email, role)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }
}
