package com.api.parcial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Stock API Service.
 *
 * <p>Arquitectura general:
 * <pre>
 * REACT Client ──HTTP/JSON──► StockController
 *                                   │
 *                            StockFacadeService
 *                           ┌───────┴────────┐
 *                       StockCache     StockProvider (Strategy)
 *                                           │
 *                                   AlphaVantageProvider ──► Alpha Vantage API
 * </pre>
 *
 * <p>Endpoints expuestos:
 * <ul>
 *   <li>{@code GET /stock/daily?symbol=AAPL}</li>
 *   <li>{@code GET /stock/intraday?symbol=AAPL}</li>
 *   <li>{@code GET /stock/weekly?symbol=AAPL}</li>
 *   <li>{@code GET /stock/monthly?symbol=AAPL}</li>
 * </ul>
 */
@SpringBootApplication
public class ApiServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(ApiServiceApplication.class);

    /**
     * Método principal de arranque de Spring Boot.
     *
     * @param args Argumentos de línea de comandos (no requeridos)
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiServiceApplication.class, args);
        logger.info("=== Stock API Service iniciado ===");
        logger.info("Prueba: http://localhost:8080/stock/daily?symbol=IBM");
    }
}