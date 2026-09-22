package com.ritesh.paymentorchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI / Swagger configuration.
 *
 * <p>Swagger UI is available at: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON spec at:         http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentOrchestratorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Orchestration Router API")
                        .description("A portfolio project demonstrating intelligent payment routing, " +
                                "gateway fallback, idempotency, and transaction persistence. " +
                                "Built with Java 17, Spring Boot 3, and PostgreSQL.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ritesh")
                                .url("https://github.com/ritesh"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
