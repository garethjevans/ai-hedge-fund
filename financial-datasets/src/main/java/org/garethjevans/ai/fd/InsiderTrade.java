package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InsiderTrade(
    @JsonProperty("ticker") String ticker,
    @JsonProperty("issuer") String issuer,
    @JsonProperty("name") String name,
    @JsonProperty("title") String title,
    @JsonProperty("is_board_director") Boolean isBoardDirector,
    @JsonProperty("transaction_date") LocalDate transactionDate,
    @JsonProperty("transaction_shares") BigDecimal transactionShares,
    @JsonProperty("transaction_price_per_share") BigDecimal transactionPricePerShare,
    @JsonProperty("transaction_value") BigDecimal transactionValue,
    @JsonProperty("shares_owned_before_transaction") BigDecimal sharesOwnedBeforeTransaction,
    @JsonProperty("shares_owned_after_transaction") BigDecimal sharesOwnedAfterTransaction,
    @JsonProperty("security_title") String securityTitle,
    @JsonProperty("filing_date") LocalDate filingDate) {}
