package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyNews(
    @JsonProperty("ticker") String ticker,
    @JsonProperty("title") String title,
    @JsonProperty("author") String author,
    @JsonProperty("source") String source,
    @JsonProperty("date") LocalDate date,
    @JsonProperty("url") String url,
    @JsonProperty("sentiment") String sentiment) {}
