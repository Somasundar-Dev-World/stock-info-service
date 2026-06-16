package com.example.stockinfo.exception;

/**
 * Thrown when a stock symbol or company name cannot be found or resolved.
 */
public class StockNotFoundException extends RuntimeException {

    private final String query;

    public StockNotFoundException(String query) {
        super("No stock data found for query: '" + query + "'");
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
