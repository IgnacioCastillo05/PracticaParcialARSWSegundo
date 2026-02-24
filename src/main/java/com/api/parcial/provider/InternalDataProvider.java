package com.api.parcial.provider;

import com.api.parcial.model.StockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Implementación interna de {@link StockProvider} que genera datos en memoria.
 *
 * <p>No depende de ninguna API externa. Útil para desarrollo, pruebas, o
 * cuando se quiere demostrar la arquitectura sin depender de servicios externos.
 *
 * <p>Demuestra el <b>Strategy Pattern</b>: basta con cambiar el bean en
 * {@link com.api.parcial.config.ProviderConfig} para usar este proveedor
 * en lugar de {@link AlphaVantageProvider}, sin modificar ninguna otra clase.
 *
 * <p>Los precios se generan con una caminata aleatoria (random walk) partiendo
 * de un precio base según el símbolo, para simular datos realistas.
 */
public class InternalDataProvider implements StockProvider {

    private static final Logger logger = LoggerFactory.getLogger(InternalDataProvider.class);

    /** Precios base por símbolo conocido. Si no existe, usa 100.0 */
    private static final Map<String, Double> BASE_PRICES = Map.of(
            "IBM",   175.0,
            "AAPL",  185.0,
            "MSFT",  415.0,
            "GOOGL", 140.0,
            "TSLA",  250.0,
            "AMZN",  180.0
    );

    /**
     * {@inheritDoc}
     *
     * <p>Genera 78 entradas (un día de trading cada 5 minutos: 9:30–16:00).
     */
    @Override
    public StockResponse getIntraday(String symbol) {
        logger.info("InternalDataProvider: generando datos intradiarios para {}", symbol);
        Map<String, Double> prices = new LinkedHashMap<>();
        double base = getBasePrice(symbol);
        Random random = seededRandom(symbol + "INTRADAY");

        LocalDateTime start = LocalDate.now().atTime(9, 30);
        for (int i = 0; i < 78; i++) {
            String timestamp = start.plusMinutes(i * 5L)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            base = nextPrice(base, random, 0.002);
            prices.put(timestamp, round(base));
        }
        return new StockResponse(symbol, "INTRADAY", prices);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Genera 100 entradas de días hábiles hacia atrás desde hoy.
     */
    @Override
    public StockResponse getDaily(String symbol) {
        logger.info("InternalDataProvider: generando datos diarios para {}", symbol);
        Map<String, Double> prices = new LinkedHashMap<>();
        double base = getBasePrice(symbol);
        Random random = seededRandom(symbol + "DAILY");

        LocalDate date = LocalDate.now();
        for (int i = 0; i < 100; i++) {
            // Saltar fines de semana
            while (date.getDayOfWeek().getValue() >= 6) {
                date = date.minusDays(1);
            }
            base = nextPrice(base, random, 0.015);
            prices.put(date.toString(), round(base));
            date = date.minusDays(1);
        }
        return new StockResponse(symbol, "DAILY", prices);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Genera 104 entradas (2 años de semanas).
     */
    @Override
    public StockResponse getWeekly(String symbol) {
        logger.info("InternalDataProvider: generando datos semanales para {}", symbol);
        Map<String, Double> prices = new LinkedHashMap<>();
        double base = getBasePrice(symbol);
        Random random = seededRandom(symbol + "WEEKLY");

        LocalDate date = LocalDate.now();
        for (int i = 0; i < 104; i++) {
            base = nextPrice(base, random, 0.03);
            prices.put(date.toString(), round(base));
            date = date.minusWeeks(1);
        }
        return new StockResponse(symbol, "WEEKLY", prices);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Genera 60 entradas (5 años de meses).
     */
    @Override
    public StockResponse getMonthly(String symbol) {
        logger.info("InternalDataProvider: generando datos mensuales para {}", symbol);
        Map<String, Double> prices = new LinkedHashMap<>();
        double base = getBasePrice(symbol);
        Random random = seededRandom(symbol + "MONTHLY");

        LocalDate date = LocalDate.now();
        for (int i = 0; i < 60; i++) {
            base = nextPrice(base, random, 0.05);
            prices.put(date.toString(), round(base));
            date = date.minusMonths(1);
        }
        return new StockResponse(symbol, "MONTHLY", prices);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Retorna el precio base para un símbolo conocido, o 100.0 si no está mapeado.
     *
     * @param symbol Ticker de la acción
     * @return Precio base en USD
     */
    private double getBasePrice(String symbol) {
        return BASE_PRICES.getOrDefault(symbol.toUpperCase(), 100.0);
    }

    /**
     * Crea un {@link Random} con semilla basada en el símbolo para que los datos
     * sean reproducibles (misma key → mismos datos en cada llamada).
     *
     * @param seed Cadena usada como semilla
     * @return Random con semilla fija
     */
    private Random seededRandom(String seed) {
        return new Random(seed.hashCode());
    }

    /**
     * Aplica una caminata aleatoria al precio actual.
     * El precio nunca baja de 1.0 para evitar valores negativos.
     *
     * @param currentPrice Precio actual
     * @param random       Generador de números aleatorios
     * @param volatility   Magnitud máxima del cambio (ej: 0.015 = ±1.5%)
     * @return Nuevo precio
     */
    private double nextPrice(double currentPrice, Random random, double volatility) {
        double change = 1.0 + (random.nextDouble() * 2 - 1) * volatility;
        return Math.max(1.0, currentPrice * change);
    }

    /**
     * Redondea un precio a 2 decimales.
     *
     * @param price Precio a redondear
     * @return Precio redondeado
     */
    private double round(double price) {
        return Math.round(price * 100.0) / 100.0;
    }
}