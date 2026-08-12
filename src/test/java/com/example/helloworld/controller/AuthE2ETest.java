package com.example.helloworld.controller;

import com.example.helloworld.domain.entities.UserEntity;
import com.example.helloworld.domain.entities.UserRole;
import com.example.helloworld.infra.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthE2ETest {

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
    void shouldLoginAndReturnAccessAndRefreshTokens() throws Exception {
        createUser("joao@example.com", UserRole.USER, true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "joao@example.com",
                                    "password": "senhaSegura123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        createUser("joao@example.com", UserRole.USER, true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "joao@example.com",
                                    "password": "senhaErrada"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Não autorizado"));
    }

    @Test
    void shouldRejectLoginWithUnknownEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "naoexiste@example.com",
                                    "password": "senhaSegura123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginOfInactiveUser() throws Exception {
        createUser("joao@example.com", UserRole.USER, false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "joao@example.com",
                                    "password": "senhaSegura123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshTokensWithValidRefreshToken() throws Exception {
        createUser("joao@example.com", UserRole.USER, true);

        String loginBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "joao@example.com",
                                    "password": "senhaSegura123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginBody).get("refreshToken").asText();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldRejectRefreshWithAccessToken() throws Exception {
        createUser("joao@example.com", UserRole.USER, true);

        String loginBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "joao@example.com",
                                    "password": "senhaSegura123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accessToken = objectMapper.readTree(loginBody).get("accessToken").asText();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "%s"
                                }
                                """.formatted(accessToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAccessToMonitorsWithoutToken() throws Exception {
        mockMvc.perform(get("/monitors"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Não autorizado"));
    }

    @Test
    void shouldForbidUserRoleFromManagingUsers() throws Exception {
        String userToken = loginToken("maria@example.com", UserRole.USER);

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));
    }

    @Test
    void shouldAllowAdminRoleToManageUsers() throws Exception {
        String adminToken = loginToken("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private void createUser(String email, UserRole role, boolean active) {
        var user = new UserEntity("Test User", email, passwordEncoder.encode("senhaSegura123"), role);
        user.setActive(active);
        userRepository.save(user);
    }

    private String loginToken(String email, UserRole role) throws Exception {
        createUser(email, role, true);
        String loginBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "senhaSegura123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(loginBody).get("accessToken").asText();
    }
}
