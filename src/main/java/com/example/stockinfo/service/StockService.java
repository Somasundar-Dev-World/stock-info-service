package com.example.stockinfo.service;

import com.example.stockinfo.exception.StockNotFoundException;
import com.example.stockinfo.model.StockResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Core service responsible for retrieving stock information.
 *
 * <p>Strategy:
 * <ol>
 *   <li>If the query looks like a ticker symbol, attempt to fetch live quote from Alpha Vantage.</li>
 *   <li>If the query looks like a company name, first resolve it to a symbol via Alpha Vantage Symbol Search,
 *       then fetch the live quote.</li>
 *   <li>If Alpha Vantage returns an error (rate limit, unsupported symbol with demo key, etc.),
 *       fall back to a built-in mock data engine with realistic data for popular equities.</li>
 * </ol>
 */
@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    @Value("${app.alphavantage.base-url}")
    private String alphaVantageBaseUrl;

    @Value("${app.alphavantage.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // =========================================================
    // Built-in Mock Data Library
    // =========================================================
    private static final Map<String, MockStock> MOCK_STOCKS = new LinkedHashMap<>();

    static {
        MOCK_STOCKS.put("AAPL",  new MockStock("AAPL",  "Apple Inc.",              "NASDAQ", "USD", 189.30, 186.50, 190.50, 185.70, 187.20, 2.10,  "+1.12%", 58_200_000L, "$2.89T", "31.2",  210.50, 164.08));
        MOCK_STOCKS.put("MSFT",  new MockStock("MSFT",  "Microsoft Corporation",   "NASDAQ", "USD", 415.80, 410.20, 418.00, 408.50, 413.00, 2.80,  "+0.68%", 21_350_000L, "$3.09T", "37.4",  430.00, 360.00));
        MOCK_STOCKS.put("GOOGL", new MockStock("GOOGL", "Alphabet Inc.",            "NASDAQ", "USD", 178.50, 175.20, 179.80, 174.00, 176.40, 2.10,  "+1.19%", 18_700_000L, "$2.20T", "23.5",  195.00, 130.00));
        MOCK_STOCKS.put("AMZN",  new MockStock("AMZN",  "Amazon.com Inc.",          "NASDAQ", "USD", 185.60, 182.00, 187.20, 181.30, 183.50, 2.10,  "+1.14%", 35_100_000L, "$1.95T", "42.1",  200.00, 118.35));
        MOCK_STOCKS.put("NVDA",  new MockStock("NVDA",  "NVIDIA Corporation",       "NASDAQ", "USD", 875.40, 860.00, 882.00, 855.00, 865.80, 9.60,  "+1.11%", 42_000_000L, "$2.15T", "65.3",  974.00, 505.00));
        MOCK_STOCKS.put("TSLA",  new MockStock("TSLA",  "Tesla Inc.",               "NASDAQ", "USD", 241.50, 235.00, 246.00, 233.50, 239.00, 2.50,  "+1.05%", 95_300_000L, "$765B",  "52.8",  278.00, 138.80));
        MOCK_STOCKS.put("META",  new MockStock("META",  "Meta Platforms Inc.",      "NASDAQ", "USD", 519.40, 514.00, 522.50, 511.00, 516.80, 2.60,  "+0.50%", 12_400_000L, "$1.31T", "27.3",  542.81, 296.37));
        MOCK_STOCKS.put("BRK.B", new MockStock("BRK.B", "Berkshire Hathaway Inc.",  "NYSE",   "USD", 412.50, 409.00, 414.20, 407.80, 410.70, 1.80,  "+0.44%",  5_800_000L, "$905B",  "21.4",  418.00, 333.00));
        MOCK_STOCKS.put("JPM",   new MockStock("JPM",   "JPMorgan Chase & Co.",     "NYSE",   "USD", 220.30, 217.50, 222.00, 216.80, 218.90, 1.40,  "+0.64%", 11_200_000L, "$637B",  "12.8",  226.30, 155.53));
        MOCK_STOCKS.put("V",     new MockStock("V",     "Visa Inc.",                "NYSE",   "USD", 279.60, 276.00, 281.20, 275.40, 277.90, 1.70,  "+0.61%",  8_900_000L, "$572B",  "30.5",  290.96, 227.00));
        MOCK_STOCKS.put("WMT",   new MockStock("WMT",   "Walmart Inc.",             "NYSE",   "USD",  72.40,  71.20,  73.10,  70.90,  71.80, 0.60,  "+0.84%", 24_100_000L, "$580B",  "35.2",   76.86,  49.86));
        MOCK_STOCKS.put("JNJ",   new MockStock("JNJ",   "Johnson & Johnson",        "NYSE",   "USD", 157.80, 155.50, 159.00, 154.80, 156.40, 1.40,  "+0.90%", 10_200_000L, "$380B",  "15.6",  165.00, 143.13));
        MOCK_STOCKS.put("NFLX",  new MockStock("NFLX",  "Netflix Inc.",             "NASDAQ", "USD", 625.70, 618.00, 629.00, 615.00, 621.50, 4.20,  "+0.68%",  6_100_000L, "$269B",  "44.7",  700.00, 344.73));
        MOCK_STOCKS.put("DIS",   new MockStock("DIS",   "The Walt Disney Company",  "NYSE",   "USD", 100.40,  98.50, 101.80,  97.80,  99.20, 1.20,  "+1.21%", 18_300_000L, "$182B",  "32.1",  123.74,  79.50));
        MOCK_STOCKS.put("IBM",   new MockStock("IBM",   "International Business Machines", "NYSE", "USD", 268.71, 272.00, 272.25, 264.80, 272.24, -3.53, "-1.30%", 7_178_779L, "$246B",  "23.5",  290.00, 155.00));
        MOCK_STOCKS.put("PYPL",  new MockStock("PYPL",  "PayPal Holdings Inc.",     "NASDAQ", "USD",  70.30,  68.50,  71.50,  68.00,  69.10, 1.20,  "+1.74%", 14_800_000L, "$75B",   "17.8",   90.00,  57.11));
        MOCK_STOCKS.put("INTC",  new MockStock("INTC",  "Intel Corporation",        "NASDAQ", "USD",  21.40,  20.80,  21.90,  20.60,  21.10, 0.30,  "+1.42%", 55_700_000L, "$91B",   "N/A",    42.65,  18.51));
        MOCK_STOCKS.put("AMD",   new MockStock("AMD",   "Advanced Micro Devices",   "NASDAQ", "USD", 158.20, 155.00, 160.00, 154.50, 156.30, 1.90,  "+1.22%", 38_000_000L, "$256B",  "N/A",   227.30, 129.00));
        MOCK_STOCKS.put("CRM",   new MockStock("CRM",   "Salesforce Inc.",          "NYSE",   "USD", 290.50, 287.00, 293.00, 285.80, 288.40, 2.10,  "+0.73%",  8_200_000L, "$281B",  "N/A",   318.71, 212.00));
        MOCK_STOCKS.put("SPOT",  new MockStock("SPOT",  "Spotify Technology S.A.", "NYSE",   "USD", 312.40, 308.00, 315.00, 307.50, 310.10, 2.30,  "+0.74%",  2_800_000L, "$63B",   "N/A",   340.00, 142.00));
        MOCK_STOCKS.put("UBER",  new MockStock("UBER",  "Uber Technologies Inc.",   "NYSE",   "USD",  77.60,  75.80,  78.50,  75.30,  76.90, 0.70,  "+0.91%", 28_000_000L, "$161B",  "N/A",    82.14,  40.05));
        MOCK_STOCKS.put("COIN",  new MockStock("COIN",  "Coinbase Global Inc.",     "NASDAQ", "USD", 224.80, 220.00, 228.00, 219.00, 222.50, 2.30,  "+1.03%",  7_000_000L, "$54B",   "N/A",   283.00, 100.00));
        MOCK_STOCKS.put("SHOP",  new MockStock("SHOP",  "Shopify Inc.",             "NYSE",   "USD",  74.30,  72.80,  75.10,  72.50,  73.60, 0.70,  "+0.95%", 12_600_000L, "$94B",   "N/A",    91.80,  47.30));
        MOCK_STOCKS.put("SNOW",  new MockStock("SNOW",  "Snowflake Inc.",           "NYSE",   "USD", 168.40, 165.00, 170.00, 164.00, 166.80, 1.60,  "+0.96%",  8_500_000L, "$57B",   "N/A",   237.72, 107.13));
    }

    // Company-name aliases for resolving name → ticker
    private static final Map<String, String> COMPANY_NAME_TO_TICKER = new LinkedHashMap<>();

    static {
        COMPANY_NAME_TO_TICKER.put("apple",      "AAPL");
        COMPANY_NAME_TO_TICKER.put("microsoft",  "MSFT");
        COMPANY_NAME_TO_TICKER.put("google",     "GOOGL");
        COMPANY_NAME_TO_TICKER.put("alphabet",   "GOOGL");
        COMPANY_NAME_TO_TICKER.put("amazon",     "AMZN");
        COMPANY_NAME_TO_TICKER.put("nvidia",     "NVDA");
        COMPANY_NAME_TO_TICKER.put("tesla",      "TSLA");
        COMPANY_NAME_TO_TICKER.put("meta",       "META");
        COMPANY_NAME_TO_TICKER.put("facebook",   "META");
        COMPANY_NAME_TO_TICKER.put("berkshire",  "BRK.B");
        COMPANY_NAME_TO_TICKER.put("jpmorgan",   "JPM");
        COMPANY_NAME_TO_TICKER.put("jp morgan",  "JPM");
        COMPANY_NAME_TO_TICKER.put("visa",       "V");
        COMPANY_NAME_TO_TICKER.put("walmart",    "WMT");
        COMPANY_NAME_TO_TICKER.put("johnson",    "JNJ");
        COMPANY_NAME_TO_TICKER.put("netflix",    "NFLX");
        COMPANY_NAME_TO_TICKER.put("disney",     "DIS");
        COMPANY_NAME_TO_TICKER.put("ibm",        "IBM");
        COMPANY_NAME_TO_TICKER.put("paypal",     "PYPL");
        COMPANY_NAME_TO_TICKER.put("intel",      "INTC");
        COMPANY_NAME_TO_TICKER.put("amd",        "AMD");
        COMPANY_NAME_TO_TICKER.put("salesforce", "CRM");
        COMPANY_NAME_TO_TICKER.put("spotify",    "SPOT");
        COMPANY_NAME_TO_TICKER.put("uber",       "UBER");
        COMPANY_NAME_TO_TICKER.put("coinbase",   "COIN");
        COMPANY_NAME_TO_TICKER.put("shopify",    "SHOP");
        COMPANY_NAME_TO_TICKER.put("snowflake",  "SNOW");
    }

    public StockService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // =========================================================
    // Public API
    // =========================================================

    /**
     * Looks up a stock by ticker symbol or company name.
     *
     * @param query the ticker symbol (e.g., "AAPL") or company name (e.g., "Apple")
     * @return a fully populated {@link StockResponse}
     * @throws StockNotFoundException if the symbol/name cannot be resolved to any stock data
     */
    public StockResponse lookupStock(String query) {
        String trimmedQuery = query.trim();
        log.info("[SERVICE] ========== BEGIN STOCK LOOKUP ==========");
        log.info("[SERVICE] Raw query received: '{}'", trimmedQuery);
        log.info("[SERVICE] Query type detection: isLikelyTicker={}", isLikelyTicker(trimmedQuery));

        // Step 1: Determine if query is ticker-like or name-like
        String resolvedTicker = resolveToTicker(trimmedQuery);
        if (resolvedTicker != null) {
            log.info("[SERVICE] Step 1 - Resolved '{}' → ticker '{}' via local alias map", trimmedQuery, resolvedTicker);
        } else {
            log.info("[SERVICE] Step 1 - Could not resolve '{}' via local alias map", trimmedQuery);
        }

        // Step 2: Try live Alpha Vantage data
        if (resolvedTicker != null) {
            log.info("[SERVICE] Step 2 - Attempting Alpha Vantage GLOBAL_QUOTE for '{}'", resolvedTicker);
            StockResponse liveResponse = tryAlphaVantageQuote(resolvedTicker);
            if (liveResponse != null) {
                log.info("[SERVICE] Step 2 - SUCCESS: Live data from Alpha Vantage for '{}' | price={}",
                        resolvedTicker, liveResponse.getPrice());
                log.info("[SERVICE] ========== END STOCK LOOKUP (Alpha Vantage Live) ==========");
                return liveResponse;
            }
            log.info("[SERVICE] Step 2 - Alpha Vantage returned no data for '{}', proceeding to fallback", resolvedTicker);
        }

        // Step 3: Try Alpha Vantage Symbol Search if it could be a name
        if (!isLikelyTicker(trimmedQuery)) {
            log.info("[SERVICE] Step 3 - Query '{}' looks like a company name, trying Alpha Vantage Symbol Search", trimmedQuery);
            String foundTicker = tryAlphaVantageSymbolSearch(trimmedQuery);
            if (foundTicker != null) {
                log.info("[SERVICE] Step 3 - Symbol Search resolved '{}' → ticker '{}'", trimmedQuery, foundTicker);
                StockResponse liveResponse = tryAlphaVantageQuote(foundTicker);
                if (liveResponse != null) {
                    log.info("[SERVICE] Step 3 - SUCCESS: Live data for searched symbol '{}' | price={}",
                            foundTicker, liveResponse.getPrice());
                    log.info("[SERVICE] ========== END STOCK LOOKUP (Alpha Vantage Symbol Search) ==========");
                    return liveResponse;
                }
                log.info("[SERVICE] Step 3 - Alpha Vantage quote failed for '{}', falling back to mock", foundTicker);
                resolvedTicker = foundTicker;
            } else {
                log.info("[SERVICE] Step 3 - Symbol Search returned no results for '{}'", trimmedQuery);
            }
        }

        // Step 4: Mock data fallback
        String tickerForMock = (resolvedTicker != null) ? resolvedTicker.toUpperCase() : trimmedQuery.toUpperCase();
        log.info("[SERVICE] Step 4 - Trying mock data engine for ticker '{}'", tickerForMock);
        StockResponse mockResponse = getMockResponse(tickerForMock, trimmedQuery);
        if (mockResponse != null) {
            log.info("[SERVICE] Step 4 - SUCCESS: Mock data returned for '{}' | symbol='{}' | price={}",
                    tickerForMock, mockResponse.getSymbol(), mockResponse.getPrice());
            log.info("[SERVICE] ========== END STOCK LOOKUP (Mock Engine) ==========");
            return mockResponse;
        }

        log.warn("[SERVICE] Step 4 - Mock engine has no data for '{}'. Throwing StockNotFoundException.", tickerForMock);
        log.info("[SERVICE] ========== END STOCK LOOKUP (NOT FOUND) ==========");
        throw new StockNotFoundException(trimmedQuery);
    }

    // =========================================================
    // Private Helpers
    // =========================================================

    /**
     * Resolves the query to a ticker symbol using the local alias map.
     */
    private String resolveToTicker(String query) {
        if (isLikelyTicker(query)) {
            log.debug("[SERVICE] '{}' matches ticker pattern, using as-is → '{}'", query, query.toUpperCase());
            return query.toUpperCase();
        }
        String lower = query.toLowerCase();
        for (Map.Entry<String, String> entry : COMPANY_NAME_TO_TICKER.entrySet()) {
            if (lower.contains(entry.getKey())) {
                log.debug("[SERVICE] Company name '{}' matched alias '{}' → ticker '{}'",
                        query, entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Heuristic: a query is "ticker-like" if it's 1-6 uppercase-compatible characters (letters, dots, hyphens).
     */
    private boolean isLikelyTicker(String query) {
        return query.matches("[A-Za-z.\\-]{1,6}");
    }

    /**
     * Attempts to fetch a live quote from Alpha Vantage GLOBAL_QUOTE endpoint.
     * Returns null on failure (rate limit, API key restriction, network error).
     */
    private StockResponse tryAlphaVantageQuote(String symbol) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(alphaVantageBaseUrl)
                    .queryParam("function", "GLOBAL_QUOTE")
                    .queryParam("symbol", symbol)
                    .queryParam("apikey", apiKey)
                    .build()
                    .toUriString();

            log.debug("[SERVICE] Alpha Vantage GLOBAL_QUOTE URL: {}", url.replace(apiKey, "***"));
            long callStart = System.currentTimeMillis();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, buildHttpEntity(), String.class);
            log.debug("[SERVICE] Alpha Vantage GLOBAL_QUOTE responded in {}ms", System.currentTimeMillis() - callStart);

            JsonNode root = objectMapper.readTree(responseEntity.getBody());

            // Log rate limit / API notes from Alpha Vantage
            if (root.has("Note")) {
                log.warn("[SERVICE] Alpha Vantage API Note (likely rate-limited): {}", root.path("Note").asText());
                return null;
            }
            if (root.has("Information")) {
                log.warn("[SERVICE] Alpha Vantage API Information message: {}", root.path("Information").asText());
                return null;
            }

            JsonNode quoteNode = root.path("Global Quote");
            if (quoteNode.isMissingNode() || quoteNode.isEmpty()) {
                log.warn("[SERVICE] Alpha Vantage returned empty 'Global Quote' for symbol '{}'. " +
                         "This usually means the demo key does not support this ticker.", symbol);
                return null;
            }

            String price = quoteNode.path("05. price").asText();
            if (price == null || price.isBlank() || price.equals("0.0000")) {
                log.warn("[SERVICE] Alpha Vantage returned zero/blank price for symbol '{}'", symbol);
                return null;
            }

            String open      = quoteNode.path("03. open").asText();
            String high      = quoteNode.path("04. high").asText();
            String low       = quoteNode.path("02. low").asText();
            String prevClose = quoteNode.path("08. previous close").asText();
            String change    = quoteNode.path("09. change").asText();
            String changePct = quoteNode.path("10. change percent").asText();
            String volume    = quoteNode.path("06. volume").asText();
            String tradingDay = quoteNode.path("07. latest trading day").asText();

            log.info("[SERVICE] Alpha Vantage GLOBAL_QUOTE data: symbol={} price={} change={} changePct={} volume={} tradingDay={}",
                    symbol, price, change, changePct, volume, tradingDay);

            MockStock mockData = MOCK_STOCKS.get(symbol.toUpperCase());
            if (mockData != null) {
                log.debug("[SERVICE] Enriching Alpha Vantage response with local metadata for '{}'", symbol);
            }

            return StockResponse.builder()
                    .symbol(quoteNode.path("01. symbol").asText(symbol))
                    .companyName(mockData != null ? mockData.companyName : symbol)
                    .exchange(mockData != null ? mockData.exchange : "N/A")
                    .currency(mockData != null ? mockData.currency : "USD")
                    .price(parseDouble(price))
                    .open(parseDouble(open))
                    .high(parseDouble(high))
                    .low(parseDouble(low))
                    .previousClose(parseDouble(prevClose))
                    .change(parseDouble(change))
                    .changePercent(changePct)
                    .volume(parseLong(volume))
                    .marketCap(mockData != null ? mockData.marketCap : null)
                    .peRatio(mockData != null ? mockData.peRatio : null)
                    .week52High(mockData != null ? mockData.week52High : null)
                    .week52Low(mockData != null ? mockData.week52Low : null)
                    .latestTradingDay(tradingDay)
                    .dataSource("Alpha Vantage (Live)")
                    .build();

        } catch (Exception e) {
            log.warn("[SERVICE] Alpha Vantage GLOBAL_QUOTE call failed for '{}': {} - {}",
                    symbol, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Uses Alpha Vantage SYMBOL_SEARCH to resolve a company name to a ticker.
     * Returns the best matching US-listed ticker, or null on failure.
     */
    private String tryAlphaVantageSymbolSearch(String companyName) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(alphaVantageBaseUrl)
                    .queryParam("function", "SYMBOL_SEARCH")
                    .queryParam("keywords", companyName)
                    .queryParam("apikey", apiKey)
                    .build()
                    .toUriString();

            log.debug("[SERVICE] Alpha Vantage SYMBOL_SEARCH URL: {}", url.replace(apiKey, "***"));
            long callStart = System.currentTimeMillis();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, buildHttpEntity(), String.class);
            log.debug("[SERVICE] Alpha Vantage SYMBOL_SEARCH responded in {}ms", System.currentTimeMillis() - callStart);

            JsonNode root = objectMapper.readTree(responseEntity.getBody());

            if (root.has("Note") || root.has("Information")) {
                log.warn("[SERVICE] Alpha Vantage SYMBOL_SEARCH rate-limited for '{}'", companyName);
                return null;
            }

            JsonNode matches = root.path("bestMatches");
            if (matches.isMissingNode() || matches.isEmpty()) {
                log.info("[SERVICE] Alpha Vantage SYMBOL_SEARCH returned no matches for '{}'", companyName);
                return null;
            }

            log.info("[SERVICE] Alpha Vantage SYMBOL_SEARCH returned {} match(es) for '{}'",
                    matches.size(), companyName);

            // Prefer United States equities with highest match score
            for (JsonNode match : matches) {
                String region = match.path("4. region").asText("");
                String type   = match.path("3. type").asText("");
                String ticker = match.path("1. symbol").asText();
                String score  = match.path("9. matchScore").asText();
                log.debug("[SERVICE]   Match candidate: symbol={} region={} type={} score={}", ticker, region, type, score);
                if ("United States".equals(region) && "Equity".equals(type)) {
                    log.info("[SERVICE] SYMBOL_SEARCH best US match: symbol='{}' score={}", ticker, score);
                    return ticker;
                }
            }
            // Fall back to first result
            String fallback = matches.get(0).path("1. symbol").asText();
            log.info("[SERVICE] SYMBOL_SEARCH falling back to first result: symbol='{}'", fallback);
            return fallback;

        } catch (Exception e) {
            log.warn("[SERVICE] Alpha Vantage SYMBOL_SEARCH failed for '{}': {} - {}",
                    companyName, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Returns a StockResponse from the built-in mock database.
     * Applies a small random daily variance to simulate live-like data.
     */
    private StockResponse getMockResponse(String ticker, String originalQuery) {
        MockStock stock = MOCK_STOCKS.get(ticker);

        if (stock == null) {
            log.debug("[SERVICE] Exact ticker '{}' not in mock DB, trying partial match", ticker);
            for (Map.Entry<String, MockStock> entry : MOCK_STOCKS.entrySet()) {
                if (entry.getKey().startsWith(ticker) || ticker.startsWith(entry.getKey())) {
                    log.debug("[SERVICE] Partial match found: '{}' → '{}'", ticker, entry.getKey());
                    stock = entry.getValue();
                    break;
                }
            }
        }

        if (stock == null) {
            log.warn("[SERVICE] Mock engine has no data for ticker '{}' (original query: '{}')", ticker, originalQuery);
            return null;
        }

        // Apply realistic daily random variance ±1.5%
        double variance = 1 + (Math.random() * 0.03 - 0.015);
        double price    = round(stock.price * variance);
        double open     = round(stock.open  * (1 + (Math.random() * 0.02 - 0.01)));
        double high     = round(Math.max(price, stock.high  * (1 + Math.random() * 0.01)));
        double low      = round(Math.min(price, stock.low   * (1 - Math.random() * 0.01)));
        double change   = round(price - stock.previousClose);
        String changePct = String.format("%.4f%%", (change / stock.previousClose) * 100);
        long volume     = (long)(stock.volume * (0.85 + Math.random() * 0.3));

        log.info("[SERVICE] Mock engine generated data: symbol='{}' price={} change={} changePct={} volume={}",
                stock.symbol, price, change, changePct, volume);

        return StockResponse.builder()
                .symbol(stock.symbol)
                .companyName(stock.companyName)
                .exchange(stock.exchange)
                .currency(stock.currency)
                .price(price)
                .open(open)
                .high(high)
                .low(low)
                .previousClose(stock.previousClose)
                .change(change)
                .changePercent(changePct)
                .volume(volume)
                .marketCap(stock.marketCap)
                .peRatio(stock.peRatio)
                .week52High(stock.week52High)
                .week52Low(stock.week52Low)
                .latestTradingDay(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .dataSource("Mock Data Engine (Alpha Vantage fallback)")
                .build();
    }

    private HttpEntity<Void> buildHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "stock-info-service/1.0");
        return new HttpEntity<>(headers);
    }

    private Double parseDouble(String value) {
        try { return (value == null || value.isBlank()) ? null : Double.parseDouble(value); }
        catch (NumberFormatException e) { return null; }
    }

    private Long parseLong(String value) {
        try { return (value == null || value.isBlank()) ? null : Long.parseLong(value); }
        catch (NumberFormatException e) { return null; }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // =========================================================
    // Inner class: MockStock record
    // =========================================================
    private static class MockStock {
        final String symbol, companyName, exchange, currency;
        final double price, open, high, low, previousClose;
        final double changeVal;
        final String changePct;
        final long volume;
        final String marketCap, peRatio;
        final double week52High, week52Low;

        MockStock(String symbol, String companyName, String exchange, String currency,
                  double price, double open, double high, double low, double previousClose,
                  double changeVal, String changePct, long volume,
                  String marketCap, String peRatio, double week52High, double week52Low) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.exchange = exchange;
            this.currency = currency;
            this.price = price;
            this.open = open;
            this.high = high;
            this.low = low;
            this.previousClose = previousClose;
            this.changeVal = changeVal;
            this.changePct = changePct;
            this.volume = volume;
            this.marketCap = marketCap;
            this.peRatio = peRatio;
            this.week52High = week52High;
            this.week52Low = week52Low;
        }
    }
}
