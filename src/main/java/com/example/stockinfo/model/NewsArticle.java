package com.example.stockinfo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsArticle {
    private String title;
    private String url;
    private String source;
    private String timePublished;
    private String summary;
    private String sentimentLabel; // e.g., Bullish, Bearish, Neutral

    public NewsArticle() {}

    public NewsArticle(String title, String url, String source, String timePublished, String summary, String sentimentLabel) {
        this.title = title;
        this.url = url;
        this.source = source;
        this.timePublished = timePublished;
        this.summary = summary;
        this.sentimentLabel = sentimentLabel;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTimePublished() { return timePublished; }
    public void setTimePublished(String timePublished) { this.timePublished = timePublished; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(String sentimentLabel) { this.sentimentLabel = sentimentLabel; }
}
