package com.api.parcial.config;

import com.api.parcial.provider.AlphaVantageProvider;
import com.api.parcial.provider.InternalDataProvider;
import com.api.parcial.provider.StockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Configuración de inyección de dependencias para el proveedor de datos.
 *
 * Para cambiar de proveedor, edita stock.provider en application.properties:
 *   "internal" → datos generados en memoria (sin dependencias externas)
 *   "alpha"    → Alpha Vantage API externa
 *
 * No es necesario modificar ninguna otra clase (Strategy Pattern).
 */
@Configuration
public class ProviderConfig {

    /**
     * Define el bean del proveedor activo según stock.provider.
     *
     * @param providerName Nombre del proveedor: "internal" o "alpha"
     * @param apiKey       API key de Alpha Vantage (solo si providerName="alpha")
     * @return Instancia del proveedor seleccionado
     */
    @Bean
    public StockProvider stockProvider(
            @Value("${stock.provider:internal}") String providerName,
            @Value("${alphavantage.api.key:demo}") String apiKey) {

        switch (providerName.toLowerCase()) {
            case "alpha":
                return new AlphaVantageProvider(apiKey);
            default:
                return new InternalDataProvider();
        }
    }
}