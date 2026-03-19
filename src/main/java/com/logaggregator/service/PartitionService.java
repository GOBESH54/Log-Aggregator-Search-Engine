package com.logaggregator.service;

import com.logaggregator.config.LogProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class PartitionService {

    private static final Logger log = LoggerFactory.getLogger(PartitionService.class);
    private static final Pattern PARTITION_NAME_PATTERN = Pattern.compile("^logs_\\d{4}_\\d{2}_\\d{2}$");

    @PersistenceContext
    private EntityManager entityManager;

    private final LogProperties logProperties;

    public PartitionService(LogProperties logProperties) {
        this.logProperties = logProperties;
    }

    @Transactional
    public void createPartition(LocalDate date) {
        String partitionName = buildPartitionName(date);
        LocalDate nextDay = date.plusDays(1);

        try {
            entityManager.createNativeQuery("SELECT create_logs_partition(:partitionName, :startDate, :endDate)")
                    .setParameter("partitionName", partitionName)
                    .setParameter("startDate", date)
                    .setParameter("endDate", nextDay)
                    .getSingleResult();
            log.info("Created partition: {}", partitionName);
        } catch (Exception e) {
            log.warn("Partition {} may already exist", partitionName, e);
        }
    }

    @Transactional
    public void dropOldPartitions() {
        int retentionDays = logProperties.getRetention().getDays();
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        String partitionName = buildPartitionName(cutoffDate);

        try {
            entityManager.createNativeQuery("SELECT drop_logs_partition(:partitionName)")
                    .setParameter("partitionName", partitionName)
                    .getSingleResult();
            log.info("Dropped old partition: {}", partitionName);
        } catch (Exception e) {
            log.error("Failed to drop partition: {}", partitionName, e);
        }
    }

    public void createFuturePartitions() {
        int createAheadDays = logProperties.getPartition().getCreateAheadDays();
        LocalDate today = LocalDate.now();
        for (int i = 0; i <= createAheadDays; i++) {
            createPartition(today.plusDays(i));
        }
    }

    private String buildPartitionName(LocalDate date) {
        String partitionName = "logs_" + date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        if (!PARTITION_NAME_PATTERN.matcher(partitionName).matches()) {
            throw new IllegalArgumentException("Invalid partition name generated");
        }
        return partitionName;
    }
}
