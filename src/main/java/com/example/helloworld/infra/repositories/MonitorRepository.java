package com.example.helloworld.infra.repositories;

import com.example.helloworld.domain.entities.monitor.MonitorEntity;
import com.example.helloworld.infra.repositories.dto.MonitorSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface MonitorRepository extends JpaRepository<MonitorEntity, UUID> {
    @Query("""
SELECT new com.example.helloworld.infra.repositories.dto.MonitorSummary(
    m.id,
    m.name.value,
    m.url.value,
    CASE
        WHEN e.httpStatusCode != 200 THEN true
        ELSE false
    END,
    e.responseTime.value,
    e.httpStatusCode
    )
FROM MonitorEntity m
INNER JOIN MonitorExecution e ON e.monitor.id = m.id
WHERE m.id = :id
ORDER BY e.checkedAt DESC
LIMIT 1
""")
    Optional<MonitorSummary> findMonitorSummary(@Param("id") UUID id);

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
