package com.example.stockinfo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO containing full stock market details for a queried symbol.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockResponse {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("open")
    private Double open;

    @JsonProperty("high")
    private Double high;

    @JsonProperty("low")
    private Double low;

    @JsonProperty("previousClose")
    private Double previousClose;

    @JsonProperty("change")
    private Double change;

    @JsonProperty("changePercent")
    private String changePercent;

    @JsonProperty("volume")
    private Long volume;

    @JsonProperty("marketCap")
    private String marketCap;

    @JsonProperty("peRatio")
    private String peRatio;

    @JsonProperty("week52High")
    private Double week52High;

    @JsonProperty("week52Low")
    private Double week52Low;

    @JsonProperty("latestTradingDay")
    private String latestTradingDay;

    @JsonProperty("dataSource")
    private String dataSource;

    @JsonProperty("news")
    private List<NewsArticle> news;

    // Private constructor - use Builder
    private StockResponse() {}

    // =========================================================
    // Builder
    // =========================================================
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final StockResponse response = new StockResponse();

        public Builder symbol(String symbol) { response.symbol = symbol; return this; }
        public Builder companyName(String companyName) { response.companyName = companyName; return this; }
        public Builder exchange(String exchange) { response.exchange = exchange; return this; }
        public Builder currency(String currency) { response.currency = currency; return this; }
        public Builder price(Double price) { response.price = price; return this; }
        public Builder open(Double open) { response.open = open; return this; }
        public Builder high(Double high) { response.high = high; return this; }
        public Builder low(Double low) { response.low = low; return this; }
        public Builder previousClose(Double previousClose) { response.previousClose = previousClose; return this; }
        public Builder change(Double change) { response.change = change; return this; }
        public Builder changePercent(String changePercent) { response.changePercent = changePercent; return this; }
        public Builder volume(Long volume) { response.volume = volume; return this; }
        public Builder marketCap(String marketCap) { response.marketCap = marketCap; return this; }
        public Builder peRatio(String peRatio) { response.peRatio = peRatio; return this; }
        public Builder week52High(Double week52High) { response.week52High = week52High; return this; }
        public Builder week52Low(Double week52Low) { response.week52Low = week52Low; return this; }
        public Builder latestTradingDay(String latestTradingDay) { response.latestTradingDay = latestTradingDay; return this; }
        public Builder dataSource(String dataSource) { response.dataSource = dataSource; return this; }
        public Builder news(List<NewsArticle> news) { response.news = news; return this; }

        public StockResponse build() { return response; }
    }

    // =========================================================
    // Getters
    // =========================================================
    public String getSymbol() { return symbol; }
    public String getCompanyName() { return companyName; }
    public String getExchange() { return exchange; }
    public String getCurrency() { return currency; }
    public Double getPrice() { return price; }
    public Double getOpen() { return open; }
    public Double getHigh() { return high; }
    public Double getLow() { return low; }
    public Double getPreviousClose() { return previousClose; }
    public Double getChange() { return change; }
    public String getChangePercent() { return changePercent; }
    public Long getVolume() { return volume; }
    public String getMarketCap() { return marketCap; }
    public String getPeRatio() { return peRatio; }
    public Double getWeek52High() { return week52High; }
    public Double getWeek52Low() { return week52Low; }
    public String getLatestTradingDay() { return latestTradingDay; }
    public String getDataSource() { return dataSource; }
    public List<NewsArticle> getNews() { return news; }
}
