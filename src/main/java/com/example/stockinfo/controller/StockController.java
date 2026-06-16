package com.example.stockinfo.controller;

import com.example.stockinfo.model.StockRequest;
import com.example.stockinfo.model.StockResponse;
import com.example.stockinfo.service.StockService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Stock Info microservice.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/v1/stocks/lookup - Look up stock details by ticker or company name</li>
 *   <li>GET  /api/v1/stocks/lookup/{symbol} - Convenience GET endpoint by ticker symbol</li>
 * </ul>
 */
@RestController
@RequestMapping(
        path = "/api/v1/stocks",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * Look up stock details by ticker symbol or company name.
     *
     * <p><b>Example Request:</b></p>
     * <pre>
     * POST /api/v1/stocks/lookup
     * Content-Type: application/json
     *
     * { "query": "AAPL" }
     *   -- or --
     * { "query": "Apple" }
     * </pre>
     *
     * @param request the request body containing the ticker symbol or company name
     * @return HTTP 200 with full stock details, or appropriate error response
     */
    @PostMapping(value = "/lookup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StockResponse> lookupStock(@Valid @RequestBody StockRequest request) {
        log.info("POST /api/v1/stocks/lookup - query: '{}'", request.getQuery());
        StockResponse response = stockService.lookupStock(request.getQuery());
        return ResponseEntity.ok(response);
    }

    /**
     * Convenience GET endpoint to look up a stock by ticker symbol in the path.
     *
     * <p><b>Example:</b> GET /api/v1/stocks/lookup/AAPL</p>
     *
     * @param symbol the ticker symbol as a path variable
     * @return HTTP 200 with full stock details, or appropriate error response
     */
    @GetMapping("/lookup/{symbol}")
    public ResponseEntity<StockResponse> lookupStockBySymbol(@PathVariable String symbol) {
        log.info("GET /api/v1/stocks/lookup/{}", symbol);
        StockResponse response = stockService.lookupStock(symbol);
        return ResponseEntity.ok(response);
    }
}
