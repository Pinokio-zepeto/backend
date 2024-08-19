package com.example.pinokkio.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Pinokio API Docs",
                description = "Pinokio 서비스의 API 명세서",
                version = "v1"
        )
)
@Configuration
public class SwaggerConfig {

    private static final String BEARER_TOKEN_PREFIX = "Bearer";

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "JWT";
        String refreshTokenSchemeName = "refresh";

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName)
                .addList(refreshTokenSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(BEARER_TOKEN_PREFIX)
                        .bearerFormat(jwtSchemeName))
                .addSecuritySchemes(refreshTokenSchemeName, new SecurityScheme()
                        .name(refreshTokenSchemeName)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}