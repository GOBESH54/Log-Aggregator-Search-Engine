package com.logaggregator.config;

import com.logaggregator.service.PartitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupConfig.class);

    private final PartitionService partitionService;

    public StartupConfig(PartitionService partitionService) {
        this.partitionService = partitionService;
    }

    @Override
    public void run(String... args) {
        log.info("Creating initial partitions");
        partitionService.createFuturePartitions();
    }
}
