package com.api.parcial.config;

import com.api.parcial.provider.AlphaVantageProvider;
import com.api.parcial.provider.StockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de inyección de dependencias para el proveedor de datos.
 *
 * <p>Implementa el punto de variación del <b>Strategy Pattern</b>: aquí se decide
 * qué implementación de {@link StockProvider} se inyecta en toda la aplicación.
 *
 * <p><b>Para cambiar de proveedor</b> (ej: de Alpha Vantage a Yahoo Finance):
 * <ol>
 *   <li>Crear {@code YahooFinanceProvider implements StockProvider}</li>
 *   <li>Cambiar el {@code return} de {@link #stockProvider} para devolver la nueva instancia</li>
 *   <li>No es necesario modificar ninguna otra clase</li>
 * </ol>
 *
 * <p><b>Para soportar múltiples proveedores simultáneos</b>, usar {@code @Bean(name="...")}
 * y {@code @Qualifier} en los puntos de inyección.
 */
@Configuration
public class ProviderConfig {

    /**
     * Define el bean del proveedor activo.
     *
     * <p>La API key se lee de la variable de entorno {@code ALPHAVANTAGE_API_KEY}
     * o del valor {@code alphavantage.api.key} en {@code application.properties}.
     * Si ninguno está presente, usa {@code "demo"} (limitado a ejemplos de IBM).
     *
     * @param apiKey Clave de Alpha Vantage inyectada por Spring
     * @return Instancia de {@link AlphaVantageProvider} como proveedor activo
     */
    @Bean
    public StockProvider stockProvider(
            @Value("${alphavantage.api.key:demo}") String apiKey) {
        return new AlphaVantageProvider(apiKey);
    }
}