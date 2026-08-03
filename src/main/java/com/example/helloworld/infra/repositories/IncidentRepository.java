package com.example.helloworld.infra.repositories;

import com.example.helloworld.domain.entities.Incident;
import com.example.helloworld.domain.entities.MonitorExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    Optional<Incident> findByMonitorId(UUID uuid);

    Optional<List<Incident>> findAllByMonitorId(UUID id);
}
