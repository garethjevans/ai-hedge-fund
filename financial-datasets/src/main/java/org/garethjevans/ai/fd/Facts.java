package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Facts(
    @JsonProperty("ticker") String ticker,
    @JsonProperty("name") String name,
    @JsonProperty("cik") String cik,
    @JsonProperty("industry") String industry,
    @JsonProperty("sector") String sector,
    @JsonProperty("category") String category,
    @JsonProperty("exchange") String exchange,
    @JsonProperty("is_active") Boolean isActive,
    @JsonProperty("listing_date") LocalDate listingDate,
    @JsonProperty("location") String location,
    @JsonProperty("market_cap") BigDecimal marketCap,
    @JsonProperty("number_of_employees") BigInteger numberOfEmployees,
    @JsonProperty("sec_filings_url") String secFilingsUrl,
    @JsonProperty("sic_code") String sicCode,
    @JsonProperty("sic_industry") String sicIndustry,
    @JsonProperty("sic_sector") String sicSector,
    @JsonProperty("website_url") String websiteUrl,
    @JsonProperty("weighted_average_shares") BigDecimal weightedAverageShares) {}
