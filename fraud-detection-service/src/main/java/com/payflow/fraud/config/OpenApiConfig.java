package com.payflow.fraud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PayFlow — Fraud Detection Service API")
                .version("1.0.0")
                .description("""
                    ## Fraud Detection Service
                    
                    Asynchronous fraud scoring engine for the PayFlow platform.
                    
                    ### How it works
                    This service operates primarily via **Kafka** — it does not expose
                    payment-processing REST endpoints. It:
                    
                    1. Consumes `payment.initiated` events from Kafka
                    2. Runs the payment through a rule engine (Amount, Velocity, Blacklist, Pattern rules)
                    3. Produces a `fraud.result` event back to Kafka
                    4. The payment-service updates the payment status accordingly
                    
                    ### Fraud rules
                    | Rule | Trigger | Risk Score |
                    |------|---------|------------|
                    | AMOUNT_RULE | > €10,000 | 0.5 |
                    | AMOUNT_RULE | > €50,000 | 0.9 |
                    | VELOCITY_RULE | > 2 tx/minute | 0.95 |
                    | VELOCITY_RULE | > 5 tx/hour | 0.7 |
                    | BLACKLIST_RULE | Known bad actor | 1.0 |
                    | PATTERN_RULE | 01:00–05:00 Amsterdam | 0.4 |
                    | PATTERN_RULE | Round amount ≥ €5,000 | 0.3 |
                    
                    ### Decision thresholds
                    - Score ≥ 0.8 → **REJECTED**
                    - Score ≥ 0.4 → **REVIEW**
                    - Score < 0.4 → **APPROVED**
                    
                    ### Admin endpoints
                    This service exposes health and metrics endpoints only.
                    """)
                .contact(new Contact().name("Ramanjaneya Reddy S").email("rama@payflow.com"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8083").description("Local development")))
            .components(new Components());
    }
}
