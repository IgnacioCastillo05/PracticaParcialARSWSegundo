package com.api.parcial.model;

import java.util.Map;

/**
 * DTO (Data Transfer Object) que representa la respuesta de datos de una acción.
 *
 * <p>Contiene el símbolo bursátil, el intervalo consultado y un mapa
 * con las fechas como clave y el precio de cierre como valor.
 *
 * <p>Ejemplo de contenido:
 * <pre>{@code
 * {
 *   "symbol":   "AAPL",
 *   "interval": "DAILY",
 *   "prices": {
 *     "2024-02-22": 182.45,
 *     "2024-02-21": 181.20
 *   }
 * }
 * }</pre>
 */
public class StockResponse {

    /** Símbolo bursátil (ticker), ej: AAPL, GOOGL, MSFT. */
    private String symbol;

    /** Intervalo de tiempo: DAILY, WEEKLY, MONTHLY o INTRADAY. */
    private String interval;

    /**
     * Mapa de fechas (o timestamps) a precios de cierre.
     * Clave: fecha en formato {@code yyyy-MM-dd} o {@code yyyy-MM-dd HH:mm:ss}.
     * Valor: precio de cierre como {@code double}.
     */
    private Map<String, Double> prices;

    /**
     * Constructor completo.
     *
     * @param symbol   Ticker de la acción
     * @param interval Etiqueta del intervalo temporal
     * @param prices   Mapa fecha → precio de cierre
     */
    public StockResponse(String symbol, String interval, Map<String, Double> prices) {
        this.symbol   = symbol;
        this.interval = interval;
        this.prices   = prices;
    }

    /** @return Símbolo bursátil */
    public String getSymbol() { return symbol; }

    /** @return Intervalo de tiempo */
    public String getInterval() { return interval; }

    /** @return Mapa fecha → precio de cierre */
    public Map<String, Double> getPrices() { return prices; }

    /** @param symbol Nuevo símbolo */
    public void setSymbol(String symbol) { this.symbol = symbol; }

    /** @param interval Nuevo intervalo */
    public void setInterval(String interval) { this.interval = interval; }

    /** @param prices Nuevo mapa de precios */
    public void setPrices(Map<String, Double> prices) { this.prices = prices; }
}