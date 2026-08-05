package com.example.helloworld.infra.repositories.dto;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public record MonitorSummary(
        UUID id,
        String name,
        String url,
        Boolean isDown,
        Long timeToResponseMillis,
        Integer httpStatus
) {
}
