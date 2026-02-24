package com.api.parcial.provider;

import com.api.parcial.model.StockResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación de {@link StockProvider} que consume la API de Alpha Vantage.
 *
 * <p>Documentación de la API: <a href="https://www.alphavantage.co/documentation/">
 * https://www.alphavantage.co/documentation/</a>
 *
 * <p>Límites del plan gratuito:
 * <ul>
 *   <li>25 requests/día</li>
 *   <li>Datos históricos completos para daily/weekly/monthly</li>
 * </ul>
 *
 * <p>Para cambiar de proveedor, crear una nueva clase que implemente
 * {@link StockProvider} y actualizar el bean en
 * {@link com.api.parcial.config.ProviderConfig}.
 */
public class AlphaVantageProvider implements StockProvider {

    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageProvider.class);
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    /** Campo JSON de precio de cierre en respuestas de Alpha Vantage */
    private static final String CLOSE_FIELD = "4. close";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    /**
     * Constructor principal.
     *
     * @param apiKey Clave de acceso a Alpha Vantage. Se inyecta desde
     *               {@code application.properties} vía
     *               {@link com.api.parcial.config.ProviderConfig}.
     */
    public AlphaVantageProvider(String apiKey) {
        this.restTemplate = new RestTemplate();
        this.objectMapper  = new ObjectMapper();
        this.apiKey        = apiKey;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code TIME_SERIES_INTRADAY} con intervalo de 5 minutos.
     */
    @Override
    public StockResponse getIntraday(String symbol) {
        String url = buildUrl("TIME_SERIES_INTRADAY", symbol, "interval=5min");
        return parseResponse(callApi(url), symbol, "INTRADAY", "Time Series (5min)");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code TIME_SERIES_DAILY}.
     */
    @Override
    public StockResponse getDaily(String symbol) {
        String url = buildUrl("TIME_SERIES_DAILY", symbol, "");
        return parseResponse(callApi(url), symbol, "DAILY", "Time Series (Daily)");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code TIME_SERIES_WEEKLY}.
     */
    @Override
    public StockResponse getWeekly(String symbol) {
        String url = buildUrl("TIME_SERIES_WEEKLY", symbol, "");
        return parseResponse(callApi(url), symbol, "WEEKLY", "Weekly Time Series");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code TIME_SERIES_MONTHLY}.
     */
    @Override
    public StockResponse getMonthly(String symbol) {
        String url = buildUrl("TIME_SERIES_MONTHLY", symbol, "");
        return parseResponse(callApi(url), symbol, "MONTHLY", "Monthly Time Series");
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Construye la URL de consulta a Alpha Vantage.
     *
     * @param function         Función de la API (TIME_SERIES_DAILY, etc.)
     * @param symbol           Ticker de la acción
     * @param additionalParams Parámetros extra (ej: {@code interval=5min})
     * @return URL completa lista para usar con {@link RestTemplate}
     */
    private String buildUrl(String function, String symbol, String additionalParams) {
        StringBuilder sb = new StringBuilder(BASE_URL)
                .append("?function=").append(function)
                .append("&symbol=").append(symbol)
                .append("&apikey=").append(apiKey);

        if (additionalParams != null && !additionalParams.isBlank()) {
            sb.append("&").append(additionalParams);
        }
        return sb.toString();
    }

    /**
     * Ejecuta la llamada HTTP GET a la URL indicada.
     *
     * @param url URL completa del endpoint
     * @return Cuerpo de la respuesta como String JSON
     * @throws RuntimeException si la llamada HTTP falla
     */
    private String callApi(String url) {
        // Log sin exponer la API key
        logger.info("Llamando a Alpha Vantage: {}", url.replaceAll("apikey=[^&]+", "apikey=***"));
        try {
            String response = restTemplate.getForObject(url, String.class);
            logger.info("Respuesta recibida de Alpha Vantage");
            return response;
        } catch (RestClientException e) {
            logger.error("Error al llamar a Alpha Vantage API", e);
            throw new RuntimeException("Error al consultar Alpha Vantage", e);
        }
    }

    /**
     * Parsea la respuesta JSON de Alpha Vantage y extrae los precios de cierre.
     *
     * <p>Alpha Vantage devuelve una estructura como:
     * <pre>{@code
     * {
     *   "Time Series (Daily)": {
     *     "2024-02-22": { "1. open": "182.0", ..., "4. close": "182.45" },
     *     ...
     *   }
     * }
     * }</pre>
     *
     * @param rawJson      JSON crudo recibido de la API
     * @param symbol       Ticker de la acción
     * @param interval     Etiqueta del intervalo (DAILY, WEEKLY, etc.)
     * @param timeSeriesKey Clave raíz de la serie temporal en el JSON
     * @return {@link StockResponse} con mapa fecha → precio de cierre
     * @throws RuntimeException si el JSON no puede parsearse o la API devuelve error
     */
    private StockResponse parseResponse(String rawJson,
                                    String symbol,
                                    String interval,
                                    String timeSeriesKey) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            // Alpha Vantage devuelve aviso en lugar de datos → retornar vacío
            if (root.has("Information") || root.has("Note")) {
                String msg = root.has("Information")
                        ? root.get("Information").asText()
                        : root.get("Note").asText();
                logger.warn("Aviso de Alpha Vantage: {}", msg);
                return new StockResponse(symbol, interval, new HashMap<>());
            }

            JsonNode series = root.path(timeSeriesKey);
            if (series.isMissingNode()) {
                logger.error("Clave '{}' no encontrada en la respuesta", timeSeriesKey);
                throw new RuntimeException("Respuesta inesperada de Alpha Vantage para: " + symbol);
            }

            Map<String, Double> prices = new HashMap<>();
            series.fields().forEachRemaining(entry -> {
                double closePrice = entry.getValue().path(CLOSE_FIELD).asDouble();
                prices.put(entry.getKey(), closePrice);
            });

            logger.info("Parseados {} precios de cierre para {} [{}]", prices.size(), symbol, interval);
            return new StockResponse(symbol, interval, prices);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parseando respuesta de Alpha Vantage", e);
            throw new RuntimeException("Error al procesar datos de " + symbol, e);
        }
    }
}