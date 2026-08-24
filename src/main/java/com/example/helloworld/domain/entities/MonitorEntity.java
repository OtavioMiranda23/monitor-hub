package com.example.helloworld.domain.entities;
import com.example.helloworld.domain.entities.valueObjects.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "monitors")
@NoArgsConstructor
@Getter
public class MonitorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Name name;

    @Column(nullable = false, length = 2048)
    private Url url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorType type;

    @Setter
    @Column(nullable = false)
    private IntervalToRun intervalToRun;

    @Column(nullable = false)
    private Timeout timeout;

    @Column(nullable = false)
    private Integer expectedStatusCode = 200;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
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

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void setNextExecution() {
        this.nextExecution = NextExecution.from(Instant.now(), this.getIntervalToRun().value());
    }

}
