package com.example.helloworld.domain.entities.monitor.converters;

import com.example.helloworld.domain.entities.monitor.valueObjects.NextExecution;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

@Converter
public class NextExecutionConverter implements AttributeConverter<NextExecution, Long> {
    @Override
    public Long convertToDatabaseColumn(NextExecution nextExecution) {
        if (nextExecution == null) return null;
        return nextExecution.value().getEpochSecond();
    }

    @Override
    public NextExecution convertToEntityAttribute(Long seconds) {
        if (seconds == null) return null;
        return new NextExecution(Instant.ofEpochSecond(seconds));
    }
}
