package com.logaggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "log")
public class LogProperties {

    private Retention retention = new Retention();
    private Partition partition = new Partition();
    private Batch batch = new Batch();
    private Ai ai = new Ai();

    public Retention getRetention() {
        return retention;
    }

    public void setRetention(Retention retention) {
        this.retention = retention;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Ai getAi() {
        return ai;
    }

    public void setAi(Ai ai) {
        this.ai = ai;
    }

    public static class Retention {
        private int days = 30;

        public int getDays() {
            return days;
        }

        public void setDays(int days) {
            this.days = days;
        }
    }

    public static class Partition {
        private int createAheadDays = 7;

        public int getCreateAheadDays() {
            return createAheadDays;
        }

        public void setCreateAheadDays(int createAheadDays) {
            this.createAheadDays = createAheadDays;
        }
    }

    public static class Batch {
        private int size = 1000;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    public static class Ai {
        private Anomaly anomaly = new Anomaly();

        public Anomaly getAnomaly() {
            return anomaly;
        }

        public void setAnomaly(Anomaly anomaly) {
            this.anomaly = anomaly;
        }
    }

    public static class Anomaly {
        private String cron = "0 0 * * * ?";
        private int maxChunkChars = 12000;

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public int getMaxChunkChars() {
            return maxChunkChars;
        }

        public void setMaxChunkChars(int maxChunkChars) {
            this.maxChunkChars = maxChunkChars;
        }
    }
}
