package com.example.helloworld.controller;

import com.example.helloworld.domain.entities.Incident;
import com.example.helloworld.infra.repositories.IncidentRepository;
import com.example.helloworld.infra.repositories.MonitorRepository;
import com.example.helloworld.service.MonitorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class VerifyUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitorService monitorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @BeforeEach
    void setup(){
        monitorRepository.deleteAll();
    }
    @Test
    void shouldCreateMonitorAndReturnHelloWorld() throws Exception {
        String requestJson = """
                {
                    "name": "Meu Monitor",
                    "url": "https://example.com"
                }
                """;
        var client = RestClient.create();
        ResponseEntity<String> response = client.post()
                .uri("http://localhost:8080/monitors")
                .contentType(APPLICATION_JSON)
                .body(requestJson)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode json = objectMapper.readTree(response.getBody());

        assertThat(json.get("id").asText())
                .isNotBlank();

        assertThat(json.get("name").asText())
                .isEqualTo("Meu Monitor");

        assertThat(json.get("url").asText())
                .isEqualTo("https://example.com");

        assertThat(json.get("type").asText())
                .isEqualTo("HTTP");

        assertThat(json.get("intervalToRunSeconds").asInt())
                .isEqualTo(300);

        assertThat(json.get("timeoutMilliseconds").asInt())
                .isEqualTo(5000);

        assertThat(json.get("expectedStatusCode").asInt())
                .isEqualTo(200);

        assertThat(json.get("active").asBoolean())
                .isTrue();

        assertThat(json.get("createdAt"))
                .isNotNull();
    }

    @Test
    void shouldCreateIncident() throws JsonProcessingException {
        String requestJson = """
                {
                    "name": "Timeout",
                    "url": "https://httpstat.us/200?sleep=10000"
                }
                """;
        var client = RestClient.create();
        ResponseEntity<String> response = client.post()
                .uri("http://localhost:8080/monitors")
                .contentType(APPLICATION_JSON)
                .body(requestJson)
                .retrieve()
                .toEntity(String.class);
        JsonNode json = objectMapper.readTree(response.getBody());
        var monitorId = json.get("id").asText();
        Incident incident = this.incidentRepository.findByMonitorId(UUID.fromString(monitorId)).orElseThrow();
        assertThat(incident.getId()).isNotNull();

    }
}
