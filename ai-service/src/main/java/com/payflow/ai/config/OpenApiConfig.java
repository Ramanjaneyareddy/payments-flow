package com.payflow.ai.config;

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
    public OpenAPI aiServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PayFlow — AI Service API")
                .version("1.0.0")
                .description("""
                    ## AI Service — Intelligent Payment Analytics
                    
                    Powered by **Spring AI 1.0.0** and **OpenAI GPT-4o-mini**.
                    
                    This service adds AI-driven intelligence on top of the PayFlow platform:
                    
                    | Endpoint | Capability |
                    |---|---|
                    | `/explain` | Natural-language explanation of any fraud decision |
                    | `/analyse` | AI risk narrative for a payment before submission |
                    | `/chat` | Conversational assistant with full payment context |
                    | `/insights` | Portfolio-level anomaly detection from stats |
                    | `/summarise` | Plain-English summary of a sender's payment history |
                    
                    ### Spring AI features demonstrated
                    - `ChatClient` fluent API with system prompts
                    - Prompt templating with `PromptTemplate`
                    - Structured output via `BeanOutputConverter`
                    - `SimpleLoggerAdvisor` for request/response logging
                    - Streaming responses (`Flux<String>`)
                    - Stateful multi-turn conversation with `MessageChatMemoryAdvisor`
                    
                    ### Authentication
                    Bearer JWT token required on all endpoints except `/health`.
                    """)
                .contact(new Contact()
                    .name("Ramanjaneya Reddy S")
                    .email("rama@payflow.com")
                    .url("https://github.com/Ramanjaneyareddy"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8087").description("Local development")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
