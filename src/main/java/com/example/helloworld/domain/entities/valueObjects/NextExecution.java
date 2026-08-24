package com.example.helloworld.domain.entities.valueObjects;

import java.time.Duration;
import java.time.Instant;

public record NextExecution(Instant value) {
    public static NextExecution from(Instant lastExecution, Duration interval) {
        if (interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("O intervalo precisa ser positivo");
        }
        return new NextExecution(lastExecution.plus(interval));
    }
}
