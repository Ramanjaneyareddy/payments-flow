package com.payflow.payment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PayFlow — Payment Service API")
                .version("1.0.0")
                .description("""
                    ## Payment Service
                    
                    Handles **payment initiation and lifecycle management** for the PayFlow platform.
                    
                    ### Key responsibilities
                    - Accept and validate incoming payment requests
                    - Persist payments with initial `PENDING` status
                    - Publish `payment.initiated` events to Kafka for fraud detection
                    - Update payment status based on fraud results (`APPROVED` / `REJECTED`)
                    - Expose payment retrieval endpoints
                    
                    ### Payment lifecycle
                    ```
                    PENDING → FRAUD_CHECK → APPROVED → COMPLETED
                                         ↘ REJECTED
                                         ↘ REVIEW (manual review required)
                    ```
                    
                    ### Authentication
                    Bearer JWT token required on all endpoints (except `/health`).
                    Obtain a token from the configured identity provider.
                    """)
                .contact(new Contact()
                    .name("Ramanjaneya Reddy S")
                    .email("rama@payflow.com")
                    .url("https://github.com/Ramanjaneyareddy"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort).description("Local development"),
                new Server().url("https://api.payflow.com").description("Production")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter JWT Bearer token. Example: `Bearer eyJhbGci...`")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .externalDocs(new ExternalDocumentation()
                .description("PayFlow GitHub Repository")
                .url("https://github.com/Ramanjaneyareddy/payflow-source"));
    }
}
