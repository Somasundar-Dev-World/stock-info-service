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
     */
    @PostMapping(value = "/lookup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StockResponse> lookupStock(@Valid @RequestBody StockRequest request) {
        long start = System.currentTimeMillis();
        log.info("[CONTROLLER] POST /api/v1/stocks/lookup | query='{}'", request.getQuery());

        StockResponse response = stockService.lookupStock(request.getQuery());

        log.info("[CONTROLLER] Returning response | symbol='{}' | company='{}' | price={} | source='{}' | took={}ms",
                response.getSymbol(),
                response.getCompanyName(),
                response.getPrice(),
                response.getDataSource(),
                System.currentTimeMillis() - start);

        return ResponseEntity.ok(response);
    }

    /**
     * Convenience GET endpoint to look up a stock by ticker symbol in the path.
     */
    @GetMapping("/lookup/{symbol}")
    public ResponseEntity<StockResponse> lookupStockBySymbol(@PathVariable String symbol) {
        long start = System.currentTimeMillis();
        log.info("[CONTROLLER] GET /api/v1/stocks/lookup/{}", symbol);

        StockResponse response = stockService.lookupStock(symbol);

        log.info("[CONTROLLER] Returning response | symbol='{}' | company='{}' | price={} | source='{}' | took={}ms",
                response.getSymbol(),
                response.getCompanyName(),
                response.getPrice(),
                response.getDataSource(),
                System.currentTimeMillis() - start);

        return ResponseEntity.ok(response);
    }
}
