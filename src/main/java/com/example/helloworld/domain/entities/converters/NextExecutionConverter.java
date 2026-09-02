package com.example.helloworld.domain.entities.converters;

import com.example.helloworld.domain.entities.valueObjects.NextExecution;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

@Converter
public class NextExecutionConverter implements AttributeConverter<NextExecution, Instant> {
    @Override
    public Instant convertToDatabaseColumn(NextExecution nextExecution) {
        if (nextExecution == null) return null;
        return nextExecution.value();
    }

    @Override
    public NextExecution convertToEntityAttribute(Instant value) {
        if (value == null) return null;
        return new NextExecution(value);
    }
}
