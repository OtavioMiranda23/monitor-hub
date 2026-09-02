package com.example.helloworld.infra.queue;

import com.example.helloworld.domain.entities.execution.ExecutionStatus;
import java.util.UUID;

public record MonitorExecutionFailedEvent(
        UUID monitorId,
        UUID executionId,
        ExecutionStatus status
) {
}
