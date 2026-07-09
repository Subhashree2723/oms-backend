package com.oms.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI docs, available at:
 *   /swagger-ui/index.html  (interactive docs)
 *   /v3/api-docs            (raw OpenAPI JSON)
 *
 * A JWT "Authorize" button is wired up so protected /api/admin/** and
 * /api/customer/** endpoints can be tested directly from the Swagger UI:
 * log in via POST /api/auth/login, copy the returned token, click
 * "Authorize" in the top-right of the Swagger UI, and paste it in
 * (just the raw token — Swagger adds the "Bearer " prefix for you).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI omsOpenApi() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("OMS Backend API")
                        .description("Online Product Order Management System - REST API")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
