package com.logaggregator.scheduler;

import com.logaggregator.service.AnomalyDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnomalyDetectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionScheduler.class);

    private final AnomalyDetectionService anomalyDetectionService;

    public AnomalyDetectionScheduler(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @Scheduled(cron = "#{@logProperties.ai.anomaly.cron}")
    public void runHourlyScan() {
        log.info("Starting hourly AI anomaly detection scan");
        anomalyDetectionService.runHourlyAnomalyScan();
        log.info("Hourly AI anomaly detection scan completed");
    }
}
