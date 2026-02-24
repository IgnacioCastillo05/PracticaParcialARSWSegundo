package com.api.parcial.service;

import com.api.parcial.cache.StockCache;
import com.api.parcial.model.StockResponse;
import com.api.parcial.provider.StockProvider;
import org.springframework.stereotype.Service;

/**
 * Servicio fachada (<b>Facade Pattern</b>) que coordina el acceso a datos de acciones.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Generar claves de cache únicas por operación</li>
 *   <li>Delegar al {@link StockCache} para evitar llamadas duplicadas</li>
 *   <li>Delegar al {@link StockProvider} activo cuando el cache no tiene el dato</li>
 * </ul>
 *
 * <p>El cliente ({@link com.api.parcial.controller.StockController}) solo interactúa
 * con esta clase, sin conocer detalles del proveedor ni del cache.
 *
 * <p><b>Extensibilidad:</b> para soportar múltiples proveedores simultáneos,
 * inyectar un {@code Map<String, StockProvider>} y seleccionar según parámetro
 * de la request.
 */
@Service
public class StockFacadeService {

    private final StockProvider provider;
    private final StockCache    cache;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param provider Implementación activa del proveedor (configurada en
     *                 {@link com.api.parcial.config.ProviderConfig})
     * @param cache    Sistema de cache en memoria
     */
    public StockFacadeService(StockProvider provider, StockCache cache) {
        this.provider = provider;
        this.cache    = cache;
    }

    /**
     * Obtiene precios diarios con cache automático.
     *
     * @param symbol Ticker de la acción
     * @return {@link StockResponse} con precios diarios
     */
    public StockResponse getDaily(String symbol) {
        return cache.getOrCompute("DAILY_" + symbol.toUpperCase(),
                () -> provider.getDaily(symbol));
    }

    /**
     * Obtiene precios intradiarios (cada 5 min) con cache automático.
     *
     * @param symbol Ticker de la acción
     * @return {@link StockResponse} con precios intradiarios
     */
    public StockResponse getIntraday(String symbol) {
        return cache.getOrCompute("INTRADAY_" + symbol.toUpperCase(),
                () -> provider.getIntraday(symbol));
    }

    /**
     * Obtiene precios semanales con cache automático.
     *
     * @param symbol Ticker de la acción
     * @return {@link StockResponse} con precios semanales
     */
    public StockResponse getWeekly(String symbol) {
        return cache.getOrCompute("WEEKLY_" + symbol.toUpperCase(),
                () -> provider.getWeekly(symbol));
    }

    /**
     * Obtiene precios mensuales con cache automático.
     *
     * @param symbol Ticker de la acción
     * @return {@link StockResponse} con precios mensuales
     */
    public StockResponse getMonthly(String symbol) {
        return cache.getOrCompute("MONTHLY_" + symbol.toUpperCase(),
                () -> provider.getMonthly(symbol));
    }
}