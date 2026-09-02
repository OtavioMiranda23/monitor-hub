package com.example.helloworld.domain.entities.monitor.converters;

import com.example.helloworld.domain.entities.monitor.valueObjects.Timeout;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter
public class TimeoutConverter implements AttributeConverter<Timeout, Duration> {

    @Override
    public Duration convertToDatabaseColumn(Timeout timeout) {
        if (timeout == null) return null;
        return timeout.value();
    }

    @Override
    public Timeout convertToEntityAttribute(Duration value) {
        if (value == null) return null;
        return new Timeout(value);
    }
}
