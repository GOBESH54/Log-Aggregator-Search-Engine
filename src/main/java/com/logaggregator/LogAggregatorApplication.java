package com.logaggregator;

import com.logaggregator.config.LogProperties;
import com.logaggregator.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({LogProperties.class, SecurityProperties.class})
public class LogAggregatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogAggregatorApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder() {
        return org.springframework.web.client.RestClient.builder();
    }
}
