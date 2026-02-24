package com.api.parcial.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cliente Java de consola para pruebas de concurrencia del servidor fachada.
 *
 * <p>Realiza múltiples peticiones HTTP simultáneas al servidor Spring Boot
 * para verificar:
 * <ul>
 *   <li>Que el servidor responde correctamente bajo carga concurrente</li>
 *   <li>Que el cache funciona (segunda ronda debe ser significativamente más rápida)</li>
 *   <li>Que no hay condiciones de carrera en el cache (ConcurrentHashMap)</li>
 * </ul>
 *
 * <p><b>Uso:</b>
 * <pre>
 *   # Con configuración por defecto (localhost:8080, 10 hilos, símbolo IBM)
 *   mvn exec:java -Dexec.mainClass="com.api.client.ConcurrentStockClient"
 *
 *   # Personalizado
 *   mvn exec:java -Dexec.mainClass="com.api.client.ConcurrentStockClient" \
 *     -Dexec.args="http://localhost:8080 AAPL 20"
 * </pre>
 */
public class ConcurrentStockClient {

    // -------------------------------------------------------------------------
    // Configuración
    // -------------------------------------------------------------------------

    /** URL base del servidor fachada */
    private static String BASE_URL = "http://localhost:8080";

    /** Símbolo a consultar en todas las pruebas */
    private static String SYMBOL = "IBM";

    /** Número de hilos concurrentes */
    private static int THREAD_COUNT = 10;

    /** Timeout por petición HTTP */
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

    // -------------------------------------------------------------------------
    // Contadores globales (thread-safe)
    // -------------------------------------------------------------------------
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // MAIN
    // =========================================================================

    /**
     * Punto de entrada del cliente concurrente.
     *
     * @param args [baseUrl] [symbol] [threadCount]  (todos opcionales)
     */
    public static void main(String[] args) throws Exception {
        parseArgs(args);

        printBanner();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();

        // -- Test 1: petición simple de cada endpoint --
        runSingleTests(httpClient);

        // -- Test 2: carga concurrente sobre /daily --
        runConcurrencyTest(httpClient, "daily",    THREAD_COUNT);
        runConcurrencyTest(httpClient, "intraday", THREAD_COUNT);
        runConcurrencyTest(httpClient, "weekly",   THREAD_COUNT);
        runConcurrencyTest(httpClient, "monthly",  THREAD_COUNT);

        // -- Test 3: segunda ronda (debe ser más rápida por el cache) --
        System.out.println("\n" + separator());
        System.out.println("  SEGUNDA RONDA (datos ya en cache → debe ser más rápido)");
        System.out.println(separator());
        runConcurrencyTest(httpClient, "daily", THREAD_COUNT);

        printSummary();
    }

    // =========================================================================
    // Tests
    // =========================================================================

    /**
     * Ejecuta una petición individual a cada endpoint y muestra el resultado.
     *
     * @param client Cliente HTTP de Java 11+
     */
    private static void runSingleTests(HttpClient client) throws Exception {
        System.out.println("\n" + separator());
        System.out.println("  TEST BÁSICO — Una petición por endpoint");
        System.out.println(separator());

        String[] endpoints = {"daily", "intraday", "weekly", "monthly"};
        for (String endpoint : endpoints) {
            String url = buildUrl(endpoint);
            System.out.printf("%-12s → %s%n", endpoint.toUpperCase(), url);

            Instant start = Instant.now();
            HttpResponse<String> response = doGet(client, url);
            long ms = Duration.between(start, Instant.now()).toMillis();

            if (response.statusCode() == 200) {
                int priceCount = countPrices(response.body());
                System.out.printf("  ✓ HTTP 200  |  %d precios  |  %d ms%n%n", priceCount, ms);
                successCount.incrementAndGet();
            } else {
                System.out.printf("  ✗ HTTP %d   |  %d ms%n", response.statusCode(), ms);
                System.out.printf("  Cuerpo: %s%n%n", response.body());
                failureCount.incrementAndGet();
            }
        }
    }

    /**
     * Lanza {@code threadCount} hilos simultáneos consultando el mismo endpoint.
     * Mide tiempos totales y por hilo.
     *
     * @param client      Cliente HTTP
     * @param endpoint    Nombre del endpoint (daily, weekly, etc.)
     * @param threadCount Número de hilos concurrentes
     */
    private static void runConcurrencyTest(HttpClient client,
                                           String endpoint,
                                           int threadCount) throws Exception {

        System.out.println("\n" + separator());
        System.out.printf("  PRUEBA CONCURRENTE — %d hilos → /stock/%s?symbol=%s%n",
                threadCount, endpoint, SYMBOL);
        System.out.println(separator());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Crear tareas
        List<Callable<RequestResult>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            tasks.add(() -> executeRequest(client, endpoint, threadId));
        }

        Instant globalStart = Instant.now();

        // Lanzar todos los hilos al mismo tiempo
        List<Future<RequestResult>> futures = executor.invokeAll(tasks);

        long totalMs = Duration.between(globalStart, Instant.now()).toMillis();

        // Recolectar resultados
        List<RequestResult> results = new ArrayList<>();
        for (Future<RequestResult> f : futures) {
            results.add(f.get());
        }

        executor.shutdown();

        // Mostrar resultados
        printConcurrencyResults(results, totalMs);
    }

    /**
     * Ejecuta una petición HTTP GET y registra el resultado.
     *
     * @param client    Cliente HTTP
     * @param endpoint  Nombre del endpoint
     * @param threadId  Identificador del hilo (para logging)
     * @return {@link RequestResult} con status, tiempo y conteo de precios
     */
    private static RequestResult executeRequest(HttpClient client,
                                                String endpoint,
                                                int threadId) {
        String url = buildUrl(endpoint);
        Instant start = Instant.now();
        try {
            HttpResponse<String> response = doGet(client, url);
            long ms = Duration.between(start, Instant.now()).toMillis();
            int prices = (response.statusCode() == 200) ? countPrices(response.body()) : 0;
            boolean ok = response.statusCode() == 200;
            if (ok) successCount.incrementAndGet(); else failureCount.incrementAndGet();
            return new RequestResult(threadId, ok, response.statusCode(), ms, prices, null);
        } catch (Exception e) {
            long ms = Duration.between(start, Instant.now()).toMillis();
            failureCount.incrementAndGet();
            return new RequestResult(threadId, false, 0, ms, 0, e.getMessage());
        }
    }

    // =========================================================================
    // Helpers HTTP
    // =========================================================================

    /**
     * Realiza una petición HTTP GET síncrona.
     *
     * @param client Cliente HTTP
     * @param url    URL completa a consultar
     * @return Respuesta HTTP con cuerpo como String
     */
    private static HttpResponse<String> doGet(HttpClient client, String url)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Cuenta el número de precios en la respuesta JSON del servidor.
     *
     * @param json JSON devuelto por el endpoint
     * @return Número de entradas en el campo {@code prices}
     */
    private static int countPrices(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode prices = root.path("prices");
            return prices.isMissingNode() ? 0 : prices.size();
        } catch (Exception e) {
            return -1;
        }
    }

    // =========================================================================
    // Presentación de resultados
    // =========================================================================

    /**
     * Imprime en consola la tabla de resultados de una prueba concurrente.
     *
     * @param results   Lista de resultados por hilo
     * @param totalMs   Tiempo total de la prueba en milisegundos
     */
    private static void printConcurrencyResults(List<RequestResult> results, long totalMs) {
        long minMs   = results.stream().mapToLong(r -> r.durationMs).min().orElse(0);
        long maxMs   = results.stream().mapToLong(r -> r.durationMs).max().orElse(0);
        double avgMs = results.stream().mapToLong(r -> r.durationMs).average().orElse(0);
        long ok      = results.stream().filter(r -> r.success).count();
        long fail    = results.stream().filter(r -> !r.success).count();

        System.out.printf("%-6s %-8s %-6s %-8s %s%n",
                "Hilo", "Estado", "HTTP", "Tiempo", "Precios/Error");
        System.out.println("─".repeat(55));
        for (RequestResult r : results) {
            if (r.success) {
                System.out.printf("%-6d %-8s %-6d %-8s %d precios%n",
                        r.threadId, "✓ OK", r.statusCode, r.durationMs + "ms", r.priceCount);
            } else {
                System.out.printf("%-6d %-8s %-6d %-8s %s%n",
                        r.threadId, "✗ FAIL", r.statusCode, r.durationMs + "ms",
                        r.error != null ? r.error : "error");
            }
        }
        System.out.println("─".repeat(55));
        System.out.printf("Resultados: %d exitosos, %d fallidos%n", ok, fail);
        System.out.printf("Tiempos:    min=%dms  max=%dms  avg=%.0fms%n", minMs, maxMs, avgMs);
        System.out.printf("Tiempo total concurrente: %d ms%n", totalMs);
    }

    /** Imprime el resumen final acumulado de todos los tests. */
    private static void printSummary() {
        System.out.println("\n" + separator());
        System.out.println("  RESUMEN FINAL");
        System.out.println(separator());
        int total = successCount.get() + failureCount.get();
        System.out.printf("  Total peticiones: %d%n", total);
        System.out.printf("  Exitosas:         %d%n", successCount.get());
        System.out.printf("  Fallidas:         %d%n", failureCount.get());
        System.out.printf("  Tasa de éxito:    %.1f%%%n",
                total > 0 ? (successCount.get() * 100.0 / total) : 0);
        System.out.println(separator());
    }

    /** Imprime el banner inicial del cliente. */
    private static void printBanner() {
        System.out.println("\n" + separator());
        System.out.println("  CLIENTE JAVA CONCURRENTE — Stock API Service");
        System.out.println(separator());
        System.out.printf("  Servidor: %s%n", BASE_URL);
        System.out.printf("  Símbolo:  %s%n", SYMBOL);
        System.out.printf("  Hilos:    %d%n", THREAD_COUNT);
        System.out.println(separator());
    }

    /** @return Línea separadora para la consola. */
    private static String separator() {
        return "═".repeat(60);
    }

    /** Construye la URL completa para un endpoint dado. */
    private static String buildUrl(String endpoint) {
        return BASE_URL + "/stock/" + endpoint + "?symbol=" + SYMBOL;
    }

    /**
     * Parsea los argumentos de línea de comandos.
     *
     * @param args [baseUrl] [symbol] [threadCount]
     */
    private static void parseArgs(String[] args) {
        if (args.length > 0) BASE_URL      = args[0];
        if (args.length > 1) SYMBOL        = args[1].toUpperCase();
        if (args.length > 2) THREAD_COUNT  = Integer.parseInt(args[2]);
    }

    // =========================================================================
    // Record interno para resultado de petición
    // =========================================================================

    /**
     * Almacena el resultado de una petición HTTP individual.
     */
    private static class RequestResult {
        final int     threadId;
        final boolean success;
        final int     statusCode;
        final long    durationMs;
        final int     priceCount;
        final String  error;

        RequestResult(int threadId, boolean success, int statusCode,
                      long durationMs, int priceCount, String error) {
            this.threadId   = threadId;
            this.success    = success;
            this.statusCode = statusCode;
            this.durationMs = durationMs;
            this.priceCount = priceCount;
            this.error      = error;
        }
    }
}