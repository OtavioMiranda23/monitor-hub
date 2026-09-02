package com.example.helloworld.domain.entities.monitor.converters;

import com.example.helloworld.domain.entities.monitor.valueObjects.IntervalToRun;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter
public class IntervalToRunConverter implements AttributeConverter<IntervalToRun, Long> {
    @Override
    public Long convertToDatabaseColumn(IntervalToRun attribute) {
        if (attribute == null) return null;
        return attribute.value().toSeconds();
    }

    @Override
    public IntervalToRun convertToEntityAttribute(Long seconds) {
        if (seconds == null) return null;
        return new IntervalToRun(Duration.ofSeconds(seconds));
    }
}
