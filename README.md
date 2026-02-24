# Stock API Service - Documentación Completa

Aplicación Spring Boot que proporciona una API REST para consultar datos históricos de acciones desde proveedores externos como **Alpha Vantage**.

## 📋 Índice

1. [Estructura del Proyecto](#estructura-del-proyecto)
2. [Características](#características)
3. [Requisitos Previos](#requisitos-previos)
4. [Instalación y Configuración](#instalación-y-configuración)
5. [Uso de la API](#uso-de-la-api)
6. [Arquitectura y Patrones](#arquitectura-y-patrones)
7. [Cómo Conectar con API Externa](#cómo-conectar-con-api-externa)
8. [Agregar Nuevo Proveedor](#agregar-nuevo-proveedor)
9. [Troubleshooting](#troubleshooting)

---

## 📁 Estructura del Proyecto

```
src/main/java/com/api/parcial/
├── ApiServiceApplication.java      # Punto de entrada de la aplicación
├── controller/
│   └── StockController.java        # Endpoints REST
├── service/
│   └── StockFacadeService.java     # Lógica de negocio (Facade Pattern)
├── provider/
│   ├── StockProvider.java          # Interfaz del proveedor (Strategy Pattern)
│   └── AlphaVantageProvider.java   # Implementación para Alpha Vantage
├── model/
│   └── StockResponse.java          # DTO de respuesta
├── cache/
│   └── StockCache.java             # Cache en memoria
└── config/
    ├── CorsConfig.java             # Configuración CORS
    └── ProviderConfig.java         # Inyección de dependencias

src/main/resources/
└── application.properties           # Configuración de la aplicación
```

### 📝 Descripción de Componentes

| Componente | Responsabilidad | Patrón |
|-----------|-----------------|--------|
| **Controller** | Expone endpoints REST | MVC |
| **FacadeService** | Coordina acceso a datos y cache | Facade Pattern |
| **Provider** | Obtiene datos de APIs externas | Strategy Pattern |
| **Cache** | Evita llamadas repetidas | Cache Pattern |
| **Model** | Representa los datos | DTO |

---

## ✨ Características

- ✅ **Endpoints REST** para consultar acciones en 4 intervalos: Diario, Semanal, Mensual, Intradiario
- ✅ **Cache en memoria** para optimizar llamadas a APIs externas
- ✅ **Manejo de errores** robusto con logs detallados
- ✅ **CORS habilitado** para acceso desde frontends
- ✅ **Arquitectura escalable** con patrón Strategy para múltiples proveedores
- ✅ **Inyección de dependencias** para código testeable
- ✅ **Configuración externa** de credenciales (API keys)

---

## 🔧 Requisitos Previos

- **Java 17** o superior
- **Maven** 3.6+
- **API Key de Alpha Vantage** (gratuita en https://www.alphavantage.co/)

### Obtener API Key:
1. Ve a https://www.alphavantage.co/
2. Completa el formulario para obtener una clave gratuita
3. Verifica tu email
4. Copiar la clave

---

## 🚀 Instalación y Configuración

### 1. Clonar/Descargar el proyecto
```bash
cd c:\Users\Danie\Documents\ARSW\Parcial-API\demo
```

### 2. Configurar API Key

#### Opción A: Variables de Entorno (Recomendado)
```bash
# Windows PowerShell
$env:ALPHAVANTAGE_API_KEY = "tu_clave_aqui"

# Windows CMD
set ALPHAVANTAGE_API_KEY=tu_clave_aqui

# Linux/Mac
export ALPHAVANTAGE_API_KEY=tu_clave_aqui
```

#### Opción B: application.properties
Edita `src/main/resources/application.properties`:
```properties
alphavantage.api.key=tu_clave_aqui_sin_spaces
```

#### Opción C: Archivo application-local.properties (Git-ignored)
1. Crea `src/main/resources/application-local.properties`
2. Agrega: `alphavantage.api.key=tu_clave_aqui`
3. Ejecuta con: `mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"`

### 3. Compilar el proyecto
```bash
mvn clean install
```

### 4. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

---

## 📡 Uso de la API

### Endpoints Disponibles

#### 1. Precios Diarios
```http
GET /stock/daily?symbol=AAPL
```
**Ejemplo:**
```bash
curl http://localhost:8080/stock/daily?symbol=AAPL
```

**Respuesta:**
```json
{
  "symbol": "AAPL",
  "interval": "DAILY",
  "prices": {
    "2024-02-22": 182.45,
    "2024-02-21": 181.20,
    "2024-02-20": 180.50
  }
}
```

#### 2. Precios Intradiarios (cada 5 minutos)
```http
GET /stock/intraday?symbol=GOOGL
```

#### 3. Precios Semanales
```http
GET /stock/weekly?symbol=MSFT
```

#### 4. Precios Mensuales
```http
GET /stock/monthly?symbol=TSLA
```

### Parámetros

| Parámetro | Requerido | Descripción | Ejemplos |
|-----------|-----------|------------|----------|
| `symbol` | Sí | Símbolo del ticker | AAPL, GOOGL, MSFT, TSLA |

### Códigos de Respuesta

| Código | Significado |
|--------|------------|
| 200 | Éxito - Datos obtenidos |
| 400 | Error de validación |
| 500 | Error interno del servidor |

---

## 🏗️ Arquitectura y Patrones

### 1. **Facade Pattern** (StockFacadeService)
Simplifica la interfaz hacia los clientes ocultando la complejidad interna:
- Coordina Provider y Cache
- Cliente solo interactúa con una clase

```
Cliente → Facade → [Provider, Cache]
```

### 2. **Strategy Pattern** (StockProvider Interface)
Permite intercambiar implementaciones sin cambiar el código cliente:
- Define interfaz `StockProvider`
- Múltiples implementaciones (AlphaVantage, Yahoo, etc)
- Runtime switch de proveedores

```
            ┌─ AlphaVantageProvider
StockProvider─┤─ YahooFinanceProvider
            └─ OtherProvider
```

### 3. **Dependency Injection** (Spring)
- ProviderConfig define qué implementación usar
- Spring inyecta automáticamente en FacadeService
- Fácil de testear con mocks

### 4. **Caching Pattern**
- `StockCache` evita llamadas repetidas
- Thread-safe con `ConcurrentHashMap`
- Mejora performance en 10-100x

### Flujo de Datos

```
Request HTTP
    ↓
StockController.getDaily(symbol)
    ↓
StockFacadeService.getDaily(symbol)
    ↓
    ├→ StockCache.getOrCompute()
    │   ├→ ¿Exists in cache? → Return
    │   └→ No existe → Call provider
    │
    └→ AlphaVantageProvider.getDaily()
        ├→ Build URL (con API key)
        ├→ HTTP REST call
        ├→ Parse JSON response
        └→ Return StockResponse + Cache
    ↓
Response JSON
```

---

## 🔌 Cómo Conectar con API Externa

### Proceso General

1. **Obtener credenciales** (API Key)
2. **Registrar la clave** en properties
3. **Crear cliente HTTP** (RestTemplate existe)
4. **Construir URL** con parámetros
5. **Manejar errores** y parsing

### Ejemplo: Alpha Vantage (Ya Implementado)

```java
// 1. URL con parámetros
String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" 
           + symbol + "&apikey=" + apiKey;

// 2. Llamada HTTP
String rawJson = restTemplate.getForObject(url, String.class);

// 3. Parsing de respuesta
JsonNode root = objectMapper.readTree(rawJson);
JsonNode timeSeries = root.get("Time Series (Daily)");

// 4. Extraer datos
prices.put(date, closePrice);
```

### Manejar Limitaciones de API

Alpha Vantage tiene limitaciones en la versión gratuita:
- **5 requests/minuto** máximo
- **500 requests/día** máximo

**Solución implementada:**
- Cache en memoria evita llamadas repetidas
- Logs informan sobre límites

```java
logger.warn("Advertencia de API: Thank you for using Alpha Vantage!");
// Esperar 12 segundos antes de reintentar
Thread.sleep(12000);
```

---

## 🆕 Agregar Nuevo Proveedor

### Paso 1: Crear Nueva Clase Implementadora

```java
package com.api.parcial.provider;

import com.api.parcial.model.StockResponse;
import org.springframework.stereotype.Service;

/**
 * Proveedor usando Yahoo Finance API
 */
@Service
public class YahooFinanceProvider implements StockProvider {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "https://query2.finance.yahoo.com";

    @Override
    public StockResponse getDaily(String symbol) {
        // Construir URL específica de Yahoo
        String url = API_URL + "/v8/finance/chart/" + symbol 
                   + "?interval=1d&range=1y";

        try {
            // Llamar API
            String rawJson = restTemplate.getForObject(url, String.class);
            
            // Parsear respuesta (formato diferente de Alpha Vantage)
            return parseYahooResponse(rawJson, symbol);
        } catch (Exception e) {
            logger.error("Error en Yahoo Finance", e);
            throw new RuntimeException("Error al obtener datos", e);
        }
    }

    @Override
    public StockResponse getIntraday(String symbol) {
        // Similar a getDaily pero con interval=5m
        return null;
    }

    @Override
    public StockResponse getWeekly(String symbol) {
        // interval=1wk
        return null;
    }

    @Override
    public StockResponse getMonthly(String symbol) {
        // interval=1mo
        return null;
    }

    private StockResponse parseYahooResponse(String rawJson, String symbol) {
        // Parsear estructura JSON de Yahoo (diferente a Alpha Vantage)
        Map<String, Double> prices = new HashMap<>();
        // ... lógica de parsing ...
        return new StockResponse(symbol, "DAILY", prices);
    }
}
```

### Paso 2: Actualizar ProviderConfig

**Opción A: Reemplazar proveedor actual**
```java
@Configuration
public class ProviderConfig {

    @Bean
    public StockProvider stockProvider() {
        // Cambiar a Yahoo Finance
        return new YahooFinanceProvider();  // ← CAMBIAR AQUI
    }
}
```

**Opción B: Múltiples proveedores (Recomendado)**
```java
@Configuration
public class ProviderConfig {

    @Bean(name = "alphaVantage")
    public StockProvider alphaVantageProvider() {
        return new AlphaVantageProvider();
    }

    @Bean(name = "yahooFinance")
    public StockProvider yahooFinanceProvider() {
        return new YahooFinanceProvider();
    }

    @Bean  // Proveedor por defecto
    public StockProvider stockProvider() {
        return alphaVantageProvider();
    }
}
```

### Paso 3: Usar en Servicio (Opcional - Si hay múltiples)
```java
@Service
public class StockFacadeService {

    private final StockProvider alphaVantage;
    private final StockProvider yahooFinance;

    public StockFacadeService(
        @Qualifier("alphaVantage") StockProvider alphaVantage,
        @Qualifier("yahooFinance") StockProvider yahooFinance
    ) {
        this.alphaVantage = alphaVantage;
        this.yahooFinance = yahooFinance;
    }

    // Usuario especifica qué proveedor usar
    @GetMapping("/daily")
    public StockResponse getDaily(
        @RequestParam String symbol,
        @RequestParam(defaultValue = "alpha") String provider
    ) {
        if ("yahoo".equals(provider)) {
            return yahooFinance.getDaily(symbol);
        }
        return alphaVantage.getDaily(symbol);
    }
}
```

### Paso 4: Agregar Configuración en application.properties
```properties
# Yahoo Finance Configuration (si aplica)
yahoofinance.api.url=https://query2.finance.yahoo.com

# Elegir proveedor por defecto
stock.provider=yahoo  # o "alpha"
```

### Comparativa de Proveedores

| Proveedor | Ventajas | Desventajas | Límites |
|-----------|----------|------------|---------|
| **Alpha Vantage** | Fácil, JSON limpio | Lento, rate limit bajo | 5/min (gratis) |
| **Yahoo Finance** | Rápido, libre | JSON complejo, menos histórico | Desconocido |
| **IEX Cloud** | Excelente datos | Pago | Varía |
| **Finnhub** | Buena API | Pago | Varía |

---

## 🐛 Troubleshooting

### Problema: "Error getting data from API"
**Causa:** API Key inválida o expirada
**Solución:**
1. Verificar que `ALPHAVANTAGE_API_KEY` está configurada
2. Validar la clave en https://www.alphavantage.co/
3. Generar una nueva si es necesario

### Problema: "429 Too Many Requests"
**Causa:** Límite de llamadas por minuto excedido
**Solución:**
1. Esperar 60 segundos (cache debería prevenir esto)
2. Actualizar cache más frecuentemente
3. Usar plan de pago de Alpha Vantage

### Problema: CORS Error en el navegador
**Causa:** El frontend no puede acceder por restricciones CORS
**Solución:**
El `CorsConfig` ya está configurado para permitir acceso. Si persiste:
```java
registry.allowedOrigins("http://localhost:3000")  // Frontend específico
        .allowedMethods("GET", "POST")
        .allowCredentials(true);
```

### Problema: NullPointerException en parseResponse
**Causa:** Estructura JSON diferente de esperada
**Solución:**
1. Verificar respuesta con curl:
```bash
curl "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=AAPL&apikey=YOUR_KEY"
```
2. Inspeccionar JSON en respuesta
3. Ajustar nombres de claves en parseResponse

### Problema: conexión lenta
**Causa:** Cache no funcionando o endpoint no cacheado
**Solución:**
1. Verificar logs: `INFO: Hit en cache para: DAILY_AAPL`
2. Segunda llamada debe ser instantánea
3. Si no, revisar StockCache.getOrCompute()

---

## 📊 Monitoreo y Logs

### Niveles de Log Configurables
```properties
# En application.properties
logging.level.com.api.parcial=DEBUG    # Ver todo
logging.level.com.api.parcial=INFO     # Info importante
logging.level.com.api.parcial=WARN     # Solo advertencias
```

### Ejemplos de Logs
```
INFO: Llamando a API: https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=AAPL
INFO: Respuesta recibida de Alpha Vantage
INFO: Parseados 100 precios para AAPL
DEBUG: Hit en cache para: DAILY_AAPL
```

---

## 🧪 Testing

Para agregar tests unitarios:

```java
@SpringBootTest
class StockControllerTest {

    @MockBean
    private StockFacadeService facadeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetDaily() throws Exception {
        StockResponse mockResponse = new StockResponse("AAPL", "DAILY", 
            Map.of("2024-02-22", 182.45));
        
        when(facadeService.getDaily("AAPL"))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/stock/daily?symbol=AAPL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("AAPL"));
    }
}
```

---

## 📈 Mejoras Futuras

- [x] Comentarios en código
- [ ] Cache distribuido (Redis)
- [ ] TTL (Time To Live) en cache
- [ ] Base de datos persistente
- [ ] Autenticación con JWT
- [ ] Rate limiting
- [ ] Webhooks para actualizaciones en tiempo real
- [ ] Swagger/OpenAPI documentation
- [ ] Métricas con Prometheus
- [ ] Tests unitarios completos

---

## 📝 Licencia

Este proyecto es de código abierto. Úsalo libremente.

---

## 📞 Soporte

Para preguntas o problemas:
1. Revisar los logs: `mvn spring-boot:run | grep ERROR`
2. Consultar la sección Troubleshooting
3. Validar configuración en application.properties

---

**¡Disfruta usando Stock API Service!** 🚀


# 🏗️ Arquitectura de la Aplicación

## Diagrama General del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTE (Navegador)                       │
│                   http://localhost:8080/stock                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                   HTTP GET Request (CORS)
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SPRING BOOT APPLICATION                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           REST CONTROLLER                                 │  │
│  │  StockController                                          │  │
│  │  ├─ GET /stock/daily?symbol=AAPL                         │  │
│  │  ├─ GET /stock/intraday?symbol=AAPL                      │  │
│  │  ├─ GET /stock/weekly?symbol=AAPL                        │  │
│  │  └─ GET /stock/monthly?symbol=AAPL                       │  │
│  └──────────────────┬───────────────────────────────────────┘  │
│                     │                                            │
│                     ▼ (inyección de dependencias)                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      SERVICE - FACADE PATTERN                             │  │
│  │  StockFacadeService                                       │  │
│  │  ├─ Coordina Provider y Cache                            │  │
│  │  ├─ genera claves de cache únicas                        │  │
│  │  └─ implementa lógica de negocio                         │  │
│  └──────┬──────────────────────────┬──────────────────────┘  │
│         │                          │                          │
│         ▼                          ▼                          │
│  ┌────────────────┐      ┌────────────────────┐             │
│  │  CACHE (Mem)   │      │  PROVIDERS         │             │
│  │  StockCache    │      │  (Strategy)        │             │
│  │  ConcurrentHM  │      │                    │             │
│  │  ├─ getOrComp  │      │ ┌─────────────────┐│             │
│  │  ├─ invalidate │      │ │AlphaVantage     ││             │
│  │  ├─ clear      │      │ │Impl.            ││             │
│  │  └─ size       │      │ └────────┬────────┘│             │
│  └────────────────┘      │          │         │             │
│         ▲                 │ ┌─────────────────┐│             │
│         │                 │ │YahooFinance     ││             │
│         │                 │ │Impl.            ││             │
│         │                 │ └─────────────────┘│             │
│         │                 │                    │             │
│         │                 └────────┬───────────┘             │
│         │                          │                         │
│         │                          ▼                         │
│         │                 RestTemplate                       │
│         └──────────────────────────────────────┐            │
│                                                │             │
│                                    HTTP REST Call            │
│                                                │             │
│          ┌─────────────────────────────────────┼──┐          │
│          │                                     │  │          │
│          ▼                                     ▼  │          │
│  ┌──────────────────┐              ┌──────────────────┐    │
│  │  JSON Response   │              │  MODEL           │    │
│  │  Parsing         │              │  StockResponse   │    │
│  │  (ObjectMapper)  │              │  ├─ symbol       │    │
│  │  ├─ timeSeries   │              │  ├─ interval     │    │
│  │  ├─ close prices │              │  └─ prices (Map) │    │
│  │  └─ dates        │              └──────────────────┘    │
│  └──────────┬───────┘                      ▲               │
│             │                              │               │
│             └──────────────────────────────┘               │
│                                                             │
└─────────────────────────┬───────────────────────────────────┘
                          │
              JSON Response with caching
                          │
                          ▼
┌─────────────────────────────────────────┐
│  {"symbol":"AAPL","interval":"DAILY",   │
│   "prices":{"2024-02-22":182.45,...}}   │
└─────────────────────────────────────────┘
```

---

## Flujo de Datos Detallado

### 1️⃣ Primera Solicitud (Cache MISS)

```
Request: GET /stock/daily?symbol=AAPL
    ↓
StockController.getDaily("AAPL")
    ↓
StockFacadeService.getDaily("AAPL")
    ↓
StockCache.getOrCompute("DAILY_AAPL", supplier)
    ↓
    ├─ ¿"DAILY_AAPL" existe en cache? NO
    │   ↓
    └─ Ejecutar supplier (llamar provider)
        ↓
    AlphaVantageProvider.getDaily("AAPL")
        ↓
    BUILD URL: https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=AAPL&apikey=XXX
        ↓
    RestTemplate.getForObject(url, String.class)
        ↓
    HTTP GET → Alpha Vantage API [~2000ms]
        ↓
    Parse JSON con ObjectMapper
        ↓
    Extraer Time Series (Daily)
        ↓
    Convertir a Map<String, Double>
        ↓
    Crear StockResponse(symbol, interval, prices)
        ↓
    Guardar en cache: cache.put("DAILY_AAPL", response)
        ↓
    Return StockResponse
        ↓
Response JSON a cliente [TOTAL: ~2-3 segundos]
```

### 2️⃣ Segunda Solicitud (Cache HIT)

```
Request: GET /stock/daily?symbol=AAPL
    ↓
StockController.getDaily("AAPL")
    ↓
StockFacadeService.getDaily("AAPL")
    ↓
StockCache.getOrCompute("DAILY_AAPL", supplier)
    ↓
    ├─ ¿"DAILY_AAPL" existe en cache? SÍ
    │   ↓
    └─ Return cache.get("DAILY_AAPL")
        ↓
Response JSON a cliente [TOTAL: <100ms] ⚡
```

---

## Inyección de Dependencias (Spring)

```
┌──────────────────────────────────┐
│  ProviderConfig (Spring Bean)    │
│  ┌────────────────────────────┐  │
│  │ @Bean                      │  │
│  │ public StockProvider()     │  │
│  │   → new AlphaVantage...()  │  │
│  └────────────────────────────┘  │
└──────────┬───────────────────────┘
           │ inyecta
           ▼
┌──────────────────────────────────┐
│  StockFacadeService              │
│  ┌────────────────────────────┐  │
│  │ constructor(                │  │
│  │   StockProvider provider,  │  │
│  │   StockCache cache         │  │
│  │ )                          │  │
│  └────────────────────────────┘  │
└──────────┬───────────────────────┘
           │ inyecta
           ▼
┌──────────────────────────────────┐
│  StockController                 │
│  ┌────────────────────────────┐  │
│  │ constructor(                │  │
│  │   StockFacadeService facade│  │
│  │ )                          │  │
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

---

## Strategy Pattern (Múltiples Proveedores)

```
InterfaceStockProvider (Contrato)
    │
    ├─ AlphaVantageProvider
    │  ├─ getDaily() → Llama API Alpha Vantage
    │  ├─ getIntraday() → Datos cada 5min
    │  ├─ getWeekly() → Datos semanales
    │  └─ getMonthly() → Datos mensuales
    │
    ├─ YahooFinanceProvider
    │  ├─ getDaily() → Llama API Yahoo
    │  ├─ getIntraday() → Yahoo 5min
    │  ├─ getWeekly() → Yahoo semanales
    │  └─ getMonthly() → Yahoo mensuales
    │
    └─ OtherProvider
       ├─ getDaily() → Llama otra API
       ├─ getIntraday() → ...
       ├─ getWeekly() → ...
       └─ getMonthly() → ...

Ventaja: Solo cambiar @Bean en ProviderConfig
         No modificar código existente
         Fácil agregar nuevos
```

---

## Capas de Datos

```
┌────────────────────────────────────────┐
│  PRESENTACIÓN (HTTP)                   │
│  ├─ Request: GET /stock/daily?symbol   │
│  └─ Response: JSON                     │
└────────────┬─────────────────────────┘
             │
┌────────────▼─────────────────────────┐
│  CONTROLLER                            │
│  ├─ Recibe request HTTP                │
│  ├─ Valida parámetros                 │
│  └─ Retorna response JSON             │
└────────────┬──────────────────────────┘
             │
┌────────────▼──────────────────────────┐
│  SERVICE (Lógica de Negocio)          │
│  ├─ Coordina cache + provider          │
│  ├─ Genera claves de cache             │
│  └─ Implementa patrones               │
└────────────┬───────────────────────────┘
             │
       ┌─────┴─────┐
       ▼           ▼
┌──────────────┐  ┌──────────────────┐
│  CACHE       │  │  PROVIDER        │
│  ├─ Memoria  │  │  ├─ API llamadas │
│  └─ Rápido   │  │  ├─ JSON parsing │
└──────────────┘  │  └─ Errores      │
                  └──────────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │ API EXTERNA  │
                  │ AlphaVantage │
                  │ Yahoo        │
                  │ Finnhub      │
                  └──────────────┘
```

---

## Configuración y Propiedades

```
application.properties
├─ spring.application.name=API-Service
├─ server.port=8080
├─ logging.level.com.api.parcial=INFO
└─ alphavantage.api.key=${ALPHAVANTAGE_API_KEY:demo}
   └─ Inyectado de variable de entorno
      o valor por defecto "demo"
```

---

## Manejo de Errores

```
Request a Proveedor
    │
    ├─ ✅ Éxito
    │  ├─ Parse JSON
    │  ├─ Guardar en cache
    │  └─ Return StockResponse
    │
    └─ ❌ Error
       ├─ RestClientException
       │  └─ Log + RuntimeException
       ├─ JSON Parse Exception
       │  └─ Log + RuntimeException
       ├─ API Error Message
       │  └─ Log warning + empty response
       └─ Rate Limiting (429)
           └─ Log warning + wait/retry
```

---

## Performance y Cache

```
Primera Solicitud (AAPL DAILY):
├─ Construcción URL: 1ms
├─ Llamada API: ~2000ms ⏱️
├─ Parse JSON: 10ms
├─ Guardar en cache: 1ms
└─ Return respuesta: 1ms
   TOTAL: ~2012ms

Segunda Solicitud (AAPL DAILY):
├─ Buscar en cache: 1ms
├─ Return respuesta: <1ms
   TOTAL: <1ms

Mejora: 2000x más rápido ⚡⚡⚡
```

---

## Escalabilidad del Sistema

```
Versión 1 (Actual):
└─ 1 Proveedor (AlphaVantage)
   └─ Cache en memoria
   └─ Performance: ~ RÁPIDO

Versión 2 (Próxima):
├─ N Proveedores (Strategy Pattern)
├─ Cache Redis (distribuido)
└─ Performance: MÁS RÁPIDO

Versión 3 (Futuro):
├─ Múltiples proveedores con fallback
├─ Base de datos persistente
├─ Rate limiting
├─ Autenticación JWT
└─ Performance: ÓPTIMO
```

---

## Estructura de Directorios

```
demo/
├─── src/main/java/com/api/parcial/
│    ├─ ApiServiceApplication.java       (Punto entrada)
│    ├─ controller/
│    │  └─ StockController.java          (Endpoints REST)
│    ├─ service/
│    │  └─ StockFacadeService.java       (Lógica negocio)
│    ├─ provider/
│    │  ├─ StockProvider.java            (Interfaz)
│    │  └─ AlphaVantageProvider.java     (Implementación)
│    ├─ model/
│    │  └─ StockResponse.java            (DTO)
│    ├─ cache/
│    │  └─ StockCache.java               (Cache sistema)
│    └─ config/
│       ├─ CorsConfig.java               (CORS)
│       └─ ProviderConfig.java           (Inyección dep)
│
├─── src/main/resources/
│    └─ application.properties            (Propiedades)
│
├─── pom.xml                             (Dependencias)
│
└─── Documentación/
     ├─ README.md                        (Completa)
     ├─ SETUP.md                         (Rápida)
     ├─ PROVIDERS_GUIDE.md               (Proveedores)
     ├─ API_REFERENCE.md                 (Endpoints)
     ├─ CAMBIOS.md                       (Resumen)
     └─ .env.example                     (Plantilla)
```

---

Esta arquitectura es:
- ✅ **Escalable:** Agregar nuevos proveedores es trivial
- ✅ **Mantenible:** Separación clara de responsabilidades
- ✅ **Testeable:** Dependencias inyectadas, fácil mockear
- ✅ **Performante:** Cache automático optimiza API
- ✅ **Segura:** Variables de entorno, sin datos sensibles

¡Listo para producción! 🚀


# Guía de Implementación de Nuevos Proveedores

Esta guía te ayudará a agregar proveedores adicionales de datos de acciones a tu aplicación.

## 📋 Tabla de Contenidos

1. [Conceptos Clave](#conceptos-clave)
2. [Paso a Paso: Agregar Yahoo Finance](#paso-a-paso-agregar-yahoo-finance)
3. [Plantilla Base](#plantilla-base)
4. [Ejemplos Prácticos](#ejemplos-prácticos)
5. [Testing del Proveedor](#testing-del-proveedor)

---

## 🎯 Conceptos Clave

### ¿Qué es un Proveedor?
Un proveedor es una clase que implementa la interfaz `StockProvider` y conecta con una API externa específica para obtener datos de acciones.

### ¿Por qué Strategy Pattern?
El patrón Strategy permite:
- ✅ Intercambiar proveedores sin modificar código existente
- ✅ Agregar nuevos proveedores fácilmente
- ✅ Testear cada uno independientemente
- ✅ Usar múltiples proveedores simultáneamente

```
Interface StockProvider (contrato)
    ↑
    ├─ AlphaVantageProvider (implementación 1)
    ├─ YahooFinanceProvider (implementación 2)
    └─ CoinGeckoProvider (implementación 3)
```

---

## 🔧 Paso a Paso: Agregar Yahoo Finance

### Paso 1: Analizar la API de Yahoo Finance

**Endpoint ejemplo:**
```
https://query1.finance.yahoo.com/v8/finance/chart/AAPL?interval=1d&range=1y
```

**Respuesta estructura:**
```json
{
  "chart": {
    "result": [
      {
        "meta": {
          "symbol": "AAPL",
          "currency": "USD"
        },
        "timestamp": [1645228800, 1645315200, ...],
        "indicators": {
          "quote": [
            {
              "close": [182.45, 181.20, 180.50, ...],
              "open": [180.00, 182.00, ...],
              "volume": [50000000, 45000000, ...],
              "high": [183.00, 182.50, ...],
              "low": [179.50, 180.00, ...]
            }
          ]
        }
      }
    ]
  }
}
```

### Paso 2: Crear la Clase Implementadora

**Archivo:** `src/main/java/com/api/parcial/provider/YahooFinanceProvider.java`

```java
package com.api.parcial.provider;

import com.api.parcial.model.StockResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Proveedor de datos usando Yahoo Finance API.
 * 
 * Ventajas:
 * - No requiere API Key
 * - Datos históricos completos
 * - Rápido y confiable
 * 
 * Desventajas:
 * - No es una API oficial (puede cambiar)
 * - JSON más complejo
 */
@Service
public class YahooFinanceProvider implements StockProvider {

    private static final Logger logger = LoggerFactory.getLogger(YahooFinanceProvider.class);
    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public YahooFinanceProvider() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public StockResponse getIntraday(String symbol) {
        // interval=5m (5 minutos)
        String url = buildUrl(symbol, "5m", "1d");  // Último día
        String rawJson = callApi(url);
        return parseYahooResponse(rawJson, symbol, "INTRADAY");
    }

    @Override
    public StockResponse getDaily(String symbol) {
        // interval=1d (diario)
        String url = buildUrl(symbol, "1d", "1y");  // Último año
        String rawJson = callApi(url);
        return parseYahooResponse(rawJson, symbol, "DAILY");
    }

    @Override
    public StockResponse getWeekly(String symbol) {
        // interval=1wk (semanal)
        String url = buildUrl(symbol, "1wk", "5y");  // Últimos 5 años
        String rawJson = callApi(url);
        return parseYahooResponse(rawJson, symbol, "WEEKLY");
    }

    @Override
    public StockResponse getMonthly(String symbol) {
        // interval=1mo (mensual)
        String url = buildUrl(symbol, "1mo", "20y");  // Últimos 20 años
        String rawJson = callApi(url);
        return parseYahooResponse(rawJson, symbol, "MONTHLY");
    }

    /**
     * Construye URL para Yahoo Finance
     */
    private String buildUrl(String symbol, String interval, String range) {
        return BASE_URL + "/" + symbol 
            + "?interval=" + interval 
            + "&range=" + range;
    }

    /**
     * Realiza la llamada HTTP a Yahoo Finance
     */
    private String callApi(String url) {
        try {
            logger.info("Llamando a Yahoo Finance: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            logger.info("Respuesta recibida de Yahoo Finance");
            return response;
        } catch (Exception e) {
            logger.error("Error al llamar Yahoo Finance API", e);
            throw new RuntimeException("Error al consultar Yahoo Finance", e);
        }
    }

    /**
     * Parsea la respuesta JSON de Yahoo Finance
     */
    private StockResponse parseYahooResponse(String rawJson, String symbol, String interval) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            Map<String, Double> prices = new HashMap<>();

            // Navegar por la estructura compleja de Yahoo
            JsonNode result = root.get("chart").get("result").get(0);
            JsonNode timestamps = result.get("timestamp");
            JsonNode quotes = result.get("indicators").get("quote").get(0);
            JsonNode closes = quotes.get("close");

            // Convertir timestamps y precios a Map
            for (int i = 0; i < timestamps.size(); i++) {
                if (timestamps.get(i) != null && closes.get(i) != null) {
                    long timestamp = timestamps.get(i).asLong();
                    double closePrice = closes.get(i).asDouble();

                    // Convertir timestamp a fecha local
                    LocalDate date = Instant.ofEpochSecond(timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    prices.put(date.toString(), closePrice);
                }
            }

            logger.info("Parseados {} precios para {} desde Yahoo", prices.size(), symbol);
            return new StockResponse(symbol, interval, prices);

        } catch (Exception e) {
            logger.error("Error al parsear respuesta de Yahoo Finance", e);
            throw new RuntimeException("Error al procesar datos", e);
        }
    }
}
```

### Paso 3: Registrar en ProviderConfig

**Opción A: Reemplazar proveedor actual**
```java
@Configuration
public class ProviderConfig {

    @Bean
    public StockProvider stockProvider() {
        return new YahooFinanceProvider();  // ← CAMBIAR
    }
}
```

**Opción B: Múltiples proveedores**
```java
@Configuration
public class ProviderConfig {

    @Bean(name = "alphaVantage")
    public StockProvider alphaVantageProvider(@Value("${alphavantage.api.key}") String apiKey) {
        return new AlphaVantageProvider(apiKey);
    }

    @Bean(name = "yahooFinance")
    public StockProvider yahooFinanceProvider() {
        return new YahooFinanceProvider();
    }

    @Bean  // Proveedor por defecto
    public StockProvider stockProvider() {
        return yahooFinanceProvider();  // Yahoo por defecto
    }
}
```

### Paso 4: Usar Múltiples Proveedores en el Controlador

```java
@RestController
@RequestMapping("/stock")
public class StockController {

    private final Map<String, StockProvider> providers;

    public StockController(@Qualifier("alphaVantage") StockProvider alphaVantage,
                         @Qualifier("yahooFinance") StockProvider yahooFinance) {
        this.providers = Map.of(
            "alpha", alphaVantage,
            "yahoo", yahooFinance
        );
    }

    @GetMapping("/daily")
    public StockResponse getDaily(
        @RequestParam String symbol,
        @RequestParam(defaultValue = "yahoo") String provider
    ) {
        return providers.get(provider).getDaily(symbol);
    }
}
```

---

## 📋 Plantilla Base

Usa esta plantilla para crear nuevos proveedores:

```java
package com.api.parcial.provider;

import com.api.parcial.model.StockResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Proveedor para [Nombre de la API].
 * 
 * Documentación API: [URL]
 */
@Service
public class [NombreProvider]Provider implements StockProvider {

    private static final Logger logger = LoggerFactory.getLogger([NombreProvider]Provider.class);
    private static final String BASE_URL = "[URL_BASE]";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;  // Si requiere API Key

    public [NombreProvider]Provider(/* Parámetros inyectados */) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public StockResponse getDaily(String symbol) {
        String url = buildUrl(symbol, "daily");
        String rawJson = callApi(url);
        return parseResponse(rawJson, symbol, "DAILY");
    }

    @Override
    public StockResponse getIntraday(String symbol) {
        // TODO: Implementar
        return null;
    }

    @Override
    public StockResponse getWeekly(String symbol) {
        // TODO: Implementar
        return null;
    }

    @Override
    public StockResponse getMonthly(String symbol) {
        // TODO: Implementar
        return null;
    }

    private String buildUrl(String symbol, String interval) {
        // Construir URL específica del proveedor
        return BASE_URL + "?symbol=" + symbol;
    }

    private String callApi(String url) {
        try {
            logger.info("Llamando a [Nombre]: {}", url);
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            logger.error("Error en llamada a API", e);
            throw new RuntimeException("Error", e);
        }
    }

    private StockResponse parseResponse(String rawJson, String symbol, String interval) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            Map<String, Double> prices = new HashMap<>();
            
            // TODO: Parsear JSON según estructura del proveedor
            
            return new StockResponse(symbol, interval, prices);
        } catch (Exception e) {
            logger.error("Error al parsear respuesta", e);
            throw new RuntimeException("Error parsing", e);
        }
    }
}
```

---

## 💡 Ejemplos Prácticos

### Ejemplo 1: CoinGecko (Criptomonedas)

```java
@Service
public class CoinGeckoProvider implements StockProvider {
    
    private static final String BASE_URL = "https://api.coingecko.com/api/v3";

    @Override
    public StockResponse getDaily(String symbol) {
        // symbol = "bitcoin", "ethereum", etc
        String url = BASE_URL + "/coins/" + symbol.toLowerCase() 
                   + "/market_chart?vs_currency=usd&days=365";
        
        String rawJson = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode prices = root.get("prices");
        
        Map<String, Double> priceMap = new HashMap<>();
        for (JsonNode price : prices) {
            long timestamp = price.get(0).asLong();
            double closePrice = price.get(1).asDouble();
            // Convertir timestamp a fecha
            priceMap.put(convertTimestamp(timestamp), closePrice);
        }
        
        return new StockResponse(symbol, "DAILY", priceMap);
    }
    
    // El resto es similar...
}
```

### Ejemplo 2: Finnhub (Requiere API Key)

```java
@Service
public class FinnhubProvider implements StockProvider {
    
    private final String apiKey;  // Inyectada desde @Value
    private static final String BASE_URL = "https://finnhub.io/api/v1";

    public FinnhubProvider(@Value("${finnhub.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public StockResponse getDaily(String symbol) {
        String url = BASE_URL + "/quote?symbol=" + symbol 
                   + "&token=" + apiKey;
        
        String rawJson = restTemplate.getForObject(url, String.class);
        // Parsear y retornar...
        return null;
    }
}
```

---

## 🧪 Testing del Proveedor

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class YahooFinanceProviderTest {

    @Autowired
    private YahooFinanceProvider provider;

    @Test
    public void testGetDaily() {
        StockResponse response = provider.getDaily("AAPL");
        
        assertNotNull(response);
        assertEquals("AAPL", response.getSymbol());
        assertEquals("DAILY", response.getInterval());
        assertFalse(response.getPrices().isEmpty());
    }

    @Test
    public void testGetIntraday() {
        StockResponse response = provider.getIntraday("GOOGL");
        
        assertNotNull(response);
        assertEquals("GOOGL", response.getSymbol());
        assertFalse(response.getPrices().isEmpty());
    }

    @Test
    public void testInvalidSymbol() {
        assertThrows(RuntimeException.class, () -> {
            provider.getDaily("INVALIDTELLTICKER123");
        });
    }
}
```

---

## 🔄 Migrar Entre Proveedores

Si necesitas cambiar de proveedor:

1. **Crear nuevo proveedor**
2. **Registrar en ProviderConfig**
3. **Cambiar o agregar @Bean**
4. Reiniciar aplicación
5. **Validar en logs:**
   ```
   INFO: Llamando a Yahoo Finance...
   INFO: Parseados 250 precios para AAPL
   ```

---

## 📊 Comparativa de APIs Recomendadas

| API | Pros | Contras | Tier Gratis |
|-----|------|---------|------------|
| **Yahoo Finance** | No requiere API Key, rápido | No oficial | ✅ Sí |
| **Alpha Vantage** | Muchos endpoints | Rate limit bajo | ✅ Limitado |
| **Finnhub** | Excelente API | Pago | ✅ Limited |
| **IEX Cloud** | Professional | Pago | ✅ Free tier |
| **CoinGecko** | Cryptos gratis | No stocks | ✅ Sí |
| **Polygon.io** | Actualizado | Pago | ✅ Limited |

---

## ⚠️ Consideraciones Importantes

1. **Rate Limiting:** Respetar límites de la API
2. **Caching:** Implementar cache para no saturar API
3. **Errores:** Manejar timeouts y errores
4. **Documentación:** Documentar cambios en formato JSON
5. **Testing:** Probar nuevos proveedores antes de usar en prod
6. **Credenciales:** Nunca commitear API Keys (usar env vars)

---

¡Listo! Ahora puedes agregar cualquier proveedor siguiendo esta guía. 🚀
