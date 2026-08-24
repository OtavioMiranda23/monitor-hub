package com.example.helloworld.domain.entities.valueObjects;

import java.time.Duration;

public record IntervalToRun(
        Duration value
) {
    public IntervalToRun {
        if (value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("O valor do intervalo deve ser maior que 0");
        }
        Duration MINIMUM_INTERVAL = java.time.Duration.ofMinutes(1);
        if (value.compareTo(MINIMUM_INTERVAL) < 0) {
            throw new IllegalArgumentException("O intervalo mínimo do intervalo deve ser de 60 segundos");
        }
    }
}
