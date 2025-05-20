package org.garethjevans.ai.mcp.financialdatasets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

public class FinancialDatasetsTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinancialDatasetsTool.class);

    private final RestClient client;

    public FinancialDatasetsTool(RestClient.Builder builder, String url, String apiKey) {
        this.client = builder
                .baseUrl(url)
                .defaultHeader("X-API-KEY",apiKey)
                .build();
    }

    @Tool(
            description = "will get company facts for a given ticker")
    public CompanyFacts companyFacts(@ToolParam(description = "the ticker to query information for") String ticker) {
        try {
            LOGGER.info("getting company facts for {}", ticker);

            CompanyFactsHolder holder = this.client
                    .get()
                    .uri("/company/facts/?ticker={ticker}", ticker)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CompanyFactsHolder.class);

            LOGGER.info("got holder {}", holder);
            return holder.companyFacts();
        } catch (Exception e) {
            LOGGER.error("error getting company facts", e);
            throw new RuntimeException(e);
        }
    }

    public record CompanyFactsHolder(
            @NotNull @JsonProperty("company_facts") CompanyFacts companyFacts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompanyFacts(
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
            @JsonProperty("sic_code")  String sicCode,
            @JsonProperty("sic_industry") String sicIndustry,
            @JsonProperty("sic_sector") String sicSector,
            @JsonProperty("website_url") String websiteUrl,
            @JsonProperty("weighted_average_shares") BigDecimal weightedAverageShares) {}
}
