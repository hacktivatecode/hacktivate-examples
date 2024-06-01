package com.example.hactivateexamples.config;


import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.temporal.ChronoUnit;

@Configuration
public class AstraConfig {

    private static final Logger log = LoggerFactory.getLogger(AstraConfig.class);

    @Value("${astra.api.application-token}")
    private String astraToken;

    @Value("${astra.database.url}")
    private String astraDbUrl;


    @Value("${astra.database.keyspace}")
    private String keyspace;


    @Bean
    public RetryConfig retryConfig() {
        return new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(200, ChronoUnit.MILLIS)
                .withExponentialBackoff()
                .build();
    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000))
                .setResponseTimeout(Timeout.ofMilliseconds(5000))
                .build();
    }


    @Bean
    public Database astraDb() {
        // Initialize the client
        DataAPIClient client = new DataAPIClient(astraToken);
        log.info("Connected to AstraDB");

        Database db = client.getDatabase(astraDbUrl, keyspace);
        log.info("Connected to Database.");

        return db;
    }
}
