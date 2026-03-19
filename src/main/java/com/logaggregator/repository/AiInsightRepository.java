package com.logaggregator.repository;

import com.logaggregator.model.AiInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiInsightRepository extends JpaRepository<AiInsight, Long> {
}
