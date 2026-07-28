package com.example.helloworld.controller;

import com.example.helloworld.infra.repositories.MonitorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VerifyUrlControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MonitorRepository monitorRepository;

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/monitors", request, String.class);

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
}
