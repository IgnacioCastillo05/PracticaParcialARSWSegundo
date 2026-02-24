package com.api.parcial.provider;

import com.api.parcial.model.StockResponse;

/**
 * Interfaz que define el contrato para proveedores de datos de acciones.
 *
 * <p>Implementa el <b>Strategy Pattern</b>: cualquier clase que implemente esta
 * interfaz puede ser intercambiada como proveedor sin modificar el código cliente.
 *
 * <p>Para agregar un nuevo proveedor (ej: Yahoo Finance, Finnhub):
 * <ol>
 *   <li>Crear clase {@code NuevoProvider implements StockProvider}</li>
 *   <li>Implementar los 4 métodos con la lógica específica de esa API</li>
 *   <li>Registrar el bean en {@link com.api.parcial.config.ProviderConfig}</li>
 * </ol>
 */
public interface StockProvider {

    /**
     * Obtiene precios intradiarios (cada 5 minutos) de una acción.
     *
     * @param symbol Símbolo del ticker (ej: AAPL, GOOGL, MSFT)
     * @return {@link StockResponse} con precios cada 5 minutos del día actual
     */
    StockResponse getIntraday(String symbol);

    /**
     * Obtiene precios de cierre diarios de una acción.
     *
     * @param symbol Símbolo del ticker
     * @return {@link StockResponse} con precios de cierre diarios
     */
    StockResponse getDaily(String symbol);

    /**
     * Obtiene precios de cierre semanales de una acción.
     *
     * @param symbol Símbolo del ticker
     * @return {@link StockResponse} con precios de cierre semanales
     */
    StockResponse getWeekly(String symbol);

    /**
     * Obtiene precios de cierre mensuales de una acción.
     *
     * @param symbol Símbolo del ticker
     * @return {@link StockResponse} con precios de cierre mensuales
     */
    StockResponse getMonthly(String symbol);
}