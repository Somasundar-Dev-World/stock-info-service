package com.example.stockinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Stock Info Service microservice.
 * <p>
 * This service provides a REST API to retrieve stock market details
 * by ticker symbol or company name using the Alpha Vantage API,
 * with an intelligent mock-data fallback for unsupported/rate-limited queries.
 * </p>
 */
@SpringBootApplication
public class StockInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockInfoApplication.class, args);
    }
}
