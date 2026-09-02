package com.example.helloworld.domain.entities.monitor;
import com.example.helloworld.domain.entities.incident.Incident;
import com.example.helloworld.domain.entities.execution.MonitorExecution;
import com.example.helloworld.domain.entities.monitor.converters.IntervalToRunConverter;
import com.example.helloworld.domain.entities.monitor.converters.NextExecutionConverter;
import com.example.helloworld.domain.entities.monitor.converters.TimeoutConverter;
import com.example.helloworld.domain.entities.monitor.valueObjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "monitors")
@NoArgsConstructor
public class MonitorEntity {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "name")
    )
    private Name name;

    @Column(nullable = false, length = 2048)
    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "url")
    )
    private Url url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorType type;

    @Column(nullable = false)
    @Convert(converter = IntervalToRunConverter.class)
    private IntervalToRun intervalToRun;

    @Column(nullable = false)
    @Convert(converter = TimeoutConverter.class)
    private Timeout timeout;

    @Column(nullable = false)
    private Integer expectedStatusCode = 200;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    @Convert(converter = NextExecutionConverter.class)
    private NextExecution nextExecution;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "monitor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MonitorExecution> executions = new ArrayList<>();

    @OneToMany(
            mappedBy = "monitor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Getter
    private List<Incident> incidents = new ArrayList<>();

    public MonitorEntity(
            Name name,
            Url url,
            MonitorType type,
            IntervalToRun intervalToRun,
            Timeout timeout

    ) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.intervalToRun = intervalToRun;
        this.timeout = timeout;
        this.setNextExecution();
    }

    public String getUrl() {
        return url.value();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void setNextExecution() {
        this.nextExecution = NextExecution.from(Instant.now(), this.getIntervalToRun());
    }

    public Duration getIntervalToRun() {
       return this.intervalToRun.value();
    }

    public Duration getTimeout() {
       return this.timeout.value();
    }

    public String getName() {
        return name.value();
    }
}
