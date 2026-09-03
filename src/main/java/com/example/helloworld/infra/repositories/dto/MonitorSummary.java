package com.example.helloworld.infra.repositories.dto;

import com.example.helloworld.domain.entities.execution.valueObjects.ResponseTime;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.UUID;

public record MonitorSummary(
        UUID id,
        String name,
        String url,
        Boolean isDown,
        ResponseTime responseTime,
        Integer httpStatus
) {
}
