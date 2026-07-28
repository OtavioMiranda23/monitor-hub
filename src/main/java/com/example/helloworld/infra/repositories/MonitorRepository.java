package com.example.helloworld.infra.repositories;

import com.example.helloworld.domain.entities.MonitorEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Repository
public interface MonitorRepository extends JpaRepository<MonitorEntity, UUID> {
    @Query("""
    SELECT m
    FROM MonitorEntity m
    WHERE m.active = true
      AND m.nextExecution <= :now
    ORDER BY m.nextExecution ASC
""")
    List<MonitorEntity> findMonitorsToScanNow(
            @Param("now") Instant now,
            Pageable pageable
    );

    Boolean existsByUrl(String url);
}
