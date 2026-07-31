package com.example.helloworld.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private MonitorEntity monitor;
    //TODO: Criar relacionamento com monitorExecutation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant resolvedAt;

    @Column(length = 2000)
    private String cause;

    @PrePersist
    protected void onCreate() {
        startedAt = Instant.now();
    }

    public Incident(MonitorEntity monitor, IncidentStatus status, Instant startedAt, String cause) {
        this.monitor = monitor;
        this.status = status;
        this.startedAt = startedAt;
        this.cause = cause;

    }
}
