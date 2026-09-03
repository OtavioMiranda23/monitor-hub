package com.example.helloworld.domain.entities.monitor.converters;

import com.example.helloworld.domain.entities.monitor.valueObjects.Timeout;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter
public class TimeoutConverter implements AttributeConverter<Timeout, Long> {

    @Override
    public Long convertToDatabaseColumn(Timeout timeout) {
        if (timeout == null) return null;
        return timeout.value().toSeconds();
    }

    @Override
    public Timeout convertToEntityAttribute(Long seconds) {
        if (seconds == null) return null;
        return new Timeout(Duration.ofSeconds(seconds));
    }
}
