package com.api.parcial.cache;

import com.api.parcial.model.StockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Cache en memoria, thread-safe, para respuestas de datos de acciones.
 *
 * <p>Evita llamadas repetidas a la API externa para el mismo símbolo e intervalo.
 * Usa {@link ConcurrentHashMap} para garantizar acceso seguro desde múltiples hilos.
 *
 * <p><b>Limitaciones actuales:</b> el cache no expira (sin TTL). Para producción
 * se recomienda migrar a Redis con expiración automática.
 *
 * <p><b>Clave de cache:</b> se construye como {@code "INTERVAL_SYMBOL"},
 * por ejemplo {@code "DAILY_AAPL"} o {@code "WEEKLY_MSFT"}.
 */
@Component
public class StockCache {

    private static final Logger logger = LoggerFactory.getLogger(StockCache.class);

    /**
     * Almacenamiento principal del cache.
     * {@link ConcurrentHashMap} garantiza operaciones atómicas sin sincronización manual.
     */
    private final ConcurrentHashMap<String, StockResponse> cache = new ConcurrentHashMap<>();

    /**
     * Devuelve el valor cacheado para la clave dada, o lo calcula y cachea si no existe.
     *
     * <p>Usa {@link ConcurrentHashMap#computeIfAbsent} para garantizar que el supplier
     * se ejecuta <em>como máximo una vez por clave</em> incluso bajo concurrencia.
     *
     * @param key      Clave única del cache (ej: {@code "DAILY_AAPL"})
     * @param supplier Función que obtiene el dato real si no está en cache
     * @return {@link StockResponse} cacheado o recién calculado
     */
    public StockResponse getOrCompute(String key, Supplier<StockResponse> supplier) {
        if (cache.containsKey(key)) {
            logger.debug("Cache HIT  → {}", key);
            return cache.get(key);
        }
        logger.debug("Cache MISS → {}. Invocando proveedor...", key);
        // computeIfAbsent es atómico: evita que dos hilos llamen al supplier a la vez
        return cache.computeIfAbsent(key, k -> supplier.get());
    }

    /**
     * Elimina una entrada específica del cache (invalidación manual).
     *
     * @param key Clave a eliminar
     */
    public void invalidate(String key) {
        logger.info("Invalidando cache para: {}", key);
        cache.remove(key);
    }

    /**
     * Elimina todas las entradas del cache.
     */
    public void clear() {
        logger.warn("Limpiando todo el cache ({} entradas)", cache.size());
        cache.clear();
    }

    /**
     * Retorna el número de entradas actualmente en cache.
     *
     * @return Tamaño del cache
     */
    public int size() {
        return cache.size();
    }
}