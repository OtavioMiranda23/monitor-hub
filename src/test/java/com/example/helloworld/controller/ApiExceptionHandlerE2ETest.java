package com.example.helloworld.controller;

import com.example.helloworld.infra.repositories.MonitorExecutionRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiExceptionHandlerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private MonitorExecutionRepository monitorExecutionRepository;

    @BeforeEach
    void setUp() {
        monitorExecutionRepository.deleteAll();
        monitorRepository.deleteAll();
    }

    @Test
    void shouldReturnBadRequestWithFieldErrorsWhenValidationFails() throws Exception {
        String invalidBody = """
                {
                    "name": "",
                    "url": "not-a-valid-url"
                }
                """;

        mockMvc.perform(post("/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").value("O nome é obrigatório"))
                .andExpect(jsonPath("$.errors[?(@.field == 'url')].message").value("A URL deve começar com http:// ou https://"));
    }

    @Test
    void shouldReturnBadRequestWhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corpo inválido"));
    }

    @Test
    void shouldReturnBadRequestWhenPathVariableHasInvalidUuid() throws Exception {
        mockMvc.perform(get("/monitors/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Argumento inválido"));
    }

    @Test
    void shouldReturnNotFoundWhenMonitorDoesNotExist() throws Exception {
        mockMvc.perform(get("/monitors/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value("Monitor not found"));
    }

    @Test
    void shouldReturnConflictWhenUrlAlreadyRegistered() throws Exception {
        String requestJson = """
                {
                    "name": "Monitor Duplicado",
                    "url": "http://127.0.0.1:9999/api"
                }
                """;

        mockMvc.perform(post("/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito"))
                .andExpect(jsonPath("$.detail").value("Já existe um monitor cadastrado com esta URL"));
    }

    @Test
    void shouldReturnProblemDetailWithTimestampAndInstance() throws Exception {
        UUID monitorId = UUID.randomUUID();
        mockMvc.perform(get("/monitors/{id}", monitorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/monitors/" + monitorId));
    }
}
