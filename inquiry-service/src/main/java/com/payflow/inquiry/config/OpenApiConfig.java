package com.payflow.inquiry.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inquiryServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PayFlow — Inquiry Service API")
                .version("1.0.0")
                .description("""
                    ## Inquiry Service
                    
                    Read-only service for searching and retrieving payment history.
                    
                    ### Key capabilities
                    - Paginated payment retrieval
                    - Filter by sender, receiver, status, currency, date range, amount range
                    - Full-text description search
                    - Advanced multi-filter search
                    - Payment statistics and sender-level analytics
                    
                    ### Notes
                    - All list endpoints return **paginated responses** (`Page<PaymentSummary>`)
                    - Default page size is **10**, maximum recommended is **100**
                    - Dates must be provided in **ISO-8601 format** (e.g. `2024-03-15T00:00:00Z`)
                    - This service is **read-only** — payment creation is handled by payment-service
                    """)
                .contact(new Contact().name("Ramanjaneya Reddy S").email("rama@payflow.com"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8085").description("Local development"),
                new Server().url("https://api.payflow.com").description("Production")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Bearer token")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
