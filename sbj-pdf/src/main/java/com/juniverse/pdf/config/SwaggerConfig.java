package com.juniverse.pdf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Java Universe
 */
@Configuration
@Slf4j
public class SwaggerConfig {

    /**
     * Configuración de OpenAPI.
     *
     * @return configuración de OpenAPI
     */
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Pdf").version("1.0.0")
                .description("Aplicacion para archivos pdf").contact(getContact()));
    }

    /**
     * Obtiene los datos de contacto.
     *
     * @return datos de contacto
     */
    private Contact getContact() {
        return new Contact().name("Java Universe")
                .url("https://github.com/programming0026");
    }
}
