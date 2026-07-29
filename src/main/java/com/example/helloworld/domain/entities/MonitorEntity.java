package com.example.helloworld.domain.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "monitors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MonitorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorType type;

    @Column(nullable = false)
    private Integer intervalToRunSeconds;

    @Column(nullable = false)
    private Integer timeoutMilliseconds;

    @Column(nullable = false)
    private Integer expectedStatusCode = 200;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Instant nextExecution;

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
            String name,
            String url,
            MonitorType type,
            Integer intervalToRunSeconds,
            Integer timeoutMilliseconds

    ) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.intervalToRunSeconds = intervalToRunSeconds;
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void setNextExecution() {
        this.nextExecution = Instant.now().plusSeconds(this.getIntervalToRunSeconds());
    }

}
