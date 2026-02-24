package com.api.parcial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS (Cross-Origin Resource Sharing).
 *
 * <p>Permite que el cliente React (corriendo en otro puerto o dominio)
 * pueda consumir esta API sin restricciones del navegador.
 *
 * <p><b>Nota de seguridad:</b> {@code allowedOrigins("*")} es adecuado para
 * desarrollo. En producción, reemplazar por el dominio exacto del frontend:
 * <pre>{@code .allowedOrigins("https://mi-frontend.azurewebsites.net")}</pre>
 */
@Configuration
public class CorsConfig {

    /**
     * Registra la política CORS para todos los endpoints de la aplicación.
     *
     * @return {@link WebMvcConfigurer} con la configuración CORS aplicada
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}