package com.example.helloworld.domain.entities.execution.valueObjects;

import java.time.Duration;

public record ResponseTime(Duration value) {
    public ResponseTime {
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("O tempo de resposta precisa ser maior que zero.");
        }
    }
}
