package com.example.helloworld.infra.queue;

import java.util.UUID;

public record MonitorExecutionResolvedEvent(
        UUID monitorId,
        UUID executionId
) {}
