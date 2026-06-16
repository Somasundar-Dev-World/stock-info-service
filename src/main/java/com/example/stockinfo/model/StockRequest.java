package com.example.stockinfo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for the stock lookup endpoint.
 * Accepts a ticker symbol (e.g., "AAPL", "IBM") or company name (e.g., "Apple", "Microsoft").
 */
public class StockRequest {

    @NotBlank(message = "Query must not be blank. Provide a ticker symbol (e.g., 'AAPL') or company name (e.g., 'Apple').")
    @Size(min = 1, max = 100, message = "Query must be between 1 and 100 characters.")
    private String query;

    // Default constructor (required for JSON deserialization)
    public StockRequest() {}

    public StockRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "StockRequest{query='" + query + "'}";
    }
}
