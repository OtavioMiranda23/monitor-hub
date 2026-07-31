package com.example.helloworld.infra.queue;

import com.example.helloworld.domain.entities.ExecutionStatus;

import java.util.UUID;

public record MonitorExecutionResolvedEvent(
        UUID monitorId,
        UUID executionId
) {
}
