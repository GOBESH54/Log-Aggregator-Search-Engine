package com.logaggregator.repository;

import com.logaggregator.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEntry, Long> {
    
    @Query(value = "SELECT * FROM logs WHERE " +
           "(CAST(:searchText AS text) IS NULL OR to_tsvector('english', message) @@ plainto_tsquery('english', :searchText)) " +
           "AND (CAST(:source AS text) IS NULL OR source = :source) " +
           "AND (CAST(:level AS text) IS NULL OR level = :level) " +
           "AND timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY timestamp DESC, id DESC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<LogEntry> searchLogs(
        @Param("searchText") String searchText,
        @Param("source") String source,
        @Param("level") String level,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("limit") int limit,
        @Param("offset") long offset
    );

    List<LogEntry> findByIdIn(List<Long> ids);

    @Query("SELECT l FROM LogEntry l " +
           "WHERE UPPER(l.level) IN :levels " +
           "AND l.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY l.timestamp ASC")
    List<LogEntry> findRecentByLevels(
        @Param("levels") List<String> levels,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
