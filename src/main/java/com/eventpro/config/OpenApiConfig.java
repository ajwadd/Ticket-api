package com.eventpro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventPro Billetterie API")
                        .version("1.0.0")
                        .description("API de billetterie : événements, réservations, paiements, billets et contrôle d'accès.")
                        .contact(new Contact()
                                .name("EventPro")
                                .email("contact@eventpro.com")));
    }
}
