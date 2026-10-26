package com.cura.common.config;

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

/**
 * OpenAPI 3 / Swagger UI configuration for CURA.
 *
 * <p>Swagger UI is accessible at: {@code http://localhost:8080/swagger-ui.html}
 * Raw OpenAPI JSON is at:        {@code http://localhost:8080/api-docs}
 *
 * <p>The JWT Bearer security scheme is declared here so that Swagger UI shows
 * an "Authorize" button — developers can paste a token and test protected endpoints
 * directly from the browser.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI curaOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development"),
                new Server().url("https://cura-api.example.com").description("Production (placeholder)")
            ))
            // Declare the JWT Bearer security scheme
            .components(new Components()
                .addSecuritySchemes(BEARER_SCHEME, bearerSecurityScheme()))
            // Apply the scheme globally — every endpoint shows the lock icon
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    private Info apiInfo() {
        return new Info()
            .title("CURA API")
            .description("""
                CURA — Clinical Unified Record & Assistance Platform
                
                ⚠ Educational prototype. Uses synthetic data. Not a validated clinical system.
                
                Authenticate via POST /api/v1/auth/login, then click "Authorize" and paste the access token.
                """)
            .version("0.1.0")
            .contact(new Contact()
                .name("Ayush Singh")
                .email("ayush@example.com"))
            .license(new License().name("Academic Use Only"));
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Paste the access token obtained from POST /api/v1/auth/login");
    }
}
