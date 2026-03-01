package com.tinyurl.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI tinyUrlOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TinyURL API")
                        .description("""
                                A URL shortening service built with Spring Boot.
                                
                                **Features:**
                                - Shorten any HTTP/HTTPS URL
                                - Optional custom aliases
                                - Optional link expiry (TTL in days)
                                - Click tracking & statistics
                                - Automatic de-duplication
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TinyURL Team")
                                .email("dev@tinyurl.local"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url(baseUrl).description("Local Development Server")
                ));
    }
}