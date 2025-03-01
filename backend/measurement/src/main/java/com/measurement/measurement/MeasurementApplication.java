package com.measurement.measurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(scanBasePackages = {"message.config", "message.consumer", "message.dto", "message.model","message.producer", "message.repository", "message.service", "utils"})
@EnableR2dbcRepositories(basePackages = {"message.repository", "repository"})
public class MeasurementApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeasurementApplication.class, args);
    }

}
