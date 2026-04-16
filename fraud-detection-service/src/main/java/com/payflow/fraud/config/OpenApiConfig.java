package com.payflow.fraud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayFlow — Fraud Detection Service")
                        .version("1.0.0")
                        .description("Asynchronous event-driven engine for payment fraud scoring. " +
                                "Processes Kafka events and emits scoring results."));
    }
}