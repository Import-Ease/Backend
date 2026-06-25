package com.example.ImportEase.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI importEaseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ImportEase API")
                        .version("1.0")
                        .description("API documentation for the ImportEase backend")
                        .contact(new Contact()
                                .name("ImportEase Team")
                                .email("esuonia4u@gmail.com")));
    }
}
