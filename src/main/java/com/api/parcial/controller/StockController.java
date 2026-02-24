package com.api.parcial.controller;

import com.api.parcial.model.StockResponse;
import com.api.parcial.service.StockFacadeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone los endpoints de consulta de acciones bursátiles.
 *
 * <p>Todos los endpoints devuelven JSON y requieren el parámetro {@code symbol}
 * con el ticker de la acción (ej: {@code AAPL}, {@code GOOGL}, {@code MSFT}).
 *
 * <p>Endpoints disponibles:
 * <ul>
 *   <li>{@code GET /stock/daily?symbol=AAPL}</li>
 *   <li>{@code GET /stock/intraday?symbol=AAPL}</li>
 *   <li>{@code GET /stock/weekly?symbol=AAPL}</li>
 *   <li>{@code GET /stock/monthly?symbol=AAPL}</li>
 * </ul>
 *
 * <p>El CORS está configurado globalmente en
 * {@link com.api.parcial.config.CorsConfig}.
 */
@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockFacadeService facade;

    /**
     * Constructor con inyección del servicio fachada.
     *
     * @param facade Servicio que coordina proveedor y cache
     */
    public StockController(StockFacadeService facade) {
        this.facade = facade;
    }

    /**
     * Retorna los precios de cierre diarios para el símbolo indicado.
     *
     * @param symbol Ticker de la acción (ej: AAPL)
     * @return JSON con símbolo, intervalo "DAILY" y mapa fecha→precio
     */
    @GetMapping("/daily")
    public StockResponse getDaily(@RequestParam String symbol) {
        return facade.getDaily(symbol);
    }

    /**
     * Retorna los precios intradiarios (cada 5 min) para el símbolo indicado.
     *
     * @param symbol Ticker de la acción
     * @return JSON con símbolo, intervalo "INTRADAY" y mapa timestamp→precio
     */
    @GetMapping("/intraday")
    public StockResponse getIntraday(@RequestParam String symbol) {
        return facade.getIntraday(symbol);
    }

    /**
     * Retorna los precios de cierre semanales para el símbolo indicado.
     *
     * @param symbol Ticker de la acción
     * @return JSON con símbolo, intervalo "WEEKLY" y mapa fecha→precio
     */
    @GetMapping("/weekly")
    public StockResponse getWeekly(@RequestParam String symbol) {
        return facade.getWeekly(symbol);
    }

    /**
     * Retorna los precios de cierre mensuales para el símbolo indicado.
     *
     * @param symbol Ticker de la acción
     * @return JSON con símbolo, intervalo "MONTHLY" y mapa fecha→precio
     */
    @GetMapping("/monthly")
    public StockResponse getMonthly(@RequestParam String symbol) {
        return facade.getMonthly(symbol);
    }
}