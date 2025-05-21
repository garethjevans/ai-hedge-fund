package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Metrics(
    @JsonProperty("ticker") String ticker,
    @JsonProperty("market_cap") BigDecimal marketCap,
    @JsonProperty("enterprise_value") BigDecimal enterpriseValue,
    @JsonProperty("price_to_earnings_ratio") BigDecimal priceToEarningsRatio,
    @JsonProperty("price_to_book_ratio") BigDecimal priceToBookRatio,
    @JsonProperty("price_to_sales_ratio") BigDecimal priceToSalesRatio,
    @JsonProperty("enterprise_value_to_ebitda_ratio") BigDecimal enterpriseValueToEbitdaRatio,
    @JsonProperty("enterprise_value_to_revenue_ratio") BigDecimal enterpriseValueToRevenueRatio,
    @JsonProperty("free_cash_flow_yield") BigDecimal freeCashFlowYield,
    @JsonProperty("peg_ratio") BigDecimal pegRatio,
    @JsonProperty("gross_margin") BigDecimal grossMargin,
    @JsonProperty("operating_margin") BigDecimal operatingMargin,
    @JsonProperty("net_margin") BigDecimal netMargin,
    @JsonProperty("return_on_equity") BigDecimal returnOnEquity,
    @JsonProperty("return_on_assets") BigDecimal returnOnAssets,
    @JsonProperty("return_on_invested_capital") BigDecimal returnOnInvestedCapital,
    @JsonProperty("asset_turnover") BigDecimal assetTurnover,
    @JsonProperty("inventory_turnover") BigDecimal inventoryTurnover,
    @JsonProperty("receivables_turnover") BigDecimal receivablesTurnover,
    @JsonProperty("days_sales_outstanding") BigDecimal daysSalesOutstanding,
    @JsonProperty("operating_cycle") BigDecimal operatingCycle,
    @JsonProperty("working_capital_turnover") BigDecimal workingCapitalTurnover,
    @JsonProperty("current_ratio") BigDecimal currentRatio,
    @JsonProperty("quick_ratio") BigDecimal quickRatio,
    @JsonProperty("cash_ratio") BigDecimal cashRatio,
    @JsonProperty("operating_cash_flow_ratio") BigDecimal operatingCashFlowRatio,
    @JsonProperty("debt_to_equity") BigDecimal debtToEquity,
    @JsonProperty("debt_to_assets") BigDecimal debtToAssets,
    @JsonProperty("interest_coverage") BigDecimal interestCoverage,
    @JsonProperty("revenue_growth") BigDecimal revenueGrowth,
    @JsonProperty("earnings_growth") BigDecimal earningsGrowth,
    @JsonProperty("book_value_growth") BigDecimal bookValueGrowth,
    @JsonProperty("earnings_per_share_growth") BigDecimal earningsPerShareGrowth,
    @JsonProperty("free_cash_flow_growth") BigDecimal freeCashFlowGrowth,
    @JsonProperty("operating_income_growth") BigDecimal operatingIncomeGrowth,
    @JsonProperty("ebitda_growth") BigDecimal ebitdaGrowth,
    @JsonProperty("payout_ratio") BigDecimal payoutRatio,
    @JsonProperty("earnings_per_share") BigDecimal earningsPerShare,
    @JsonProperty("book_value_per_share") BigDecimal bookValuePerShare,
    @JsonProperty("free_cash_flow_per_share") BigDecimal freeCashFlowPerShare) {}
