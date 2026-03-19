package com.logaggregator.scheduler;

import com.logaggregator.service.PartitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetentionScheduler.class);

    private final PartitionService partitionService;

    public RetentionScheduler(PartitionService partitionService) {
        this.partitionService = partitionService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void enforceRetentionPolicy() {
        log.info("Starting retention policy enforcement");
        partitionService.dropOldPartitions();
        partitionService.createFuturePartitions();
        log.info("Retention policy enforcement completed");
    }
}
