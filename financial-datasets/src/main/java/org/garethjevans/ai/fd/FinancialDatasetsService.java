package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class FinancialDatasetsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FinancialDatasetsService.class);

  private final RestClient client;

  public FinancialDatasetsService(RestClient.Builder builder, String url, String apiKey) {
    this.client = builder.baseUrl(url).defaultHeader("X-API-KEY", apiKey).build();
  }

  public CompanyFacts companyFacts(String ticker) {
    LOGGER.info("getting company facts for {}", ticker);

    CompanyFactsHolder holder =
        this.client
            .get()
            .uri("/company/facts/?ticker={ticker}", ticker)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(CompanyFactsHolder.class);

    LOGGER.info("got company facts holder {}", holder);
    return holder.companyFacts();
  }

  public List<Price> getPrices(String ticker, LocalDate startDate, LocalDate endDate) {
    LOGGER.info("getting prices for {} between {} and {}", ticker, startDate, endDate);
    return this.client
        .get()
        .uri(
            "/prices/?ticker={ticker}&interval=day&interval_multiplier=1&start_date={startDate}&end_date={endDate}",
            ticker,
            startDate,
            endDate)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(PricesHolder.class)
        .prices();
  }

  public List<Metric> getFinancialMetrics(
      String ticker, LocalDate endDate, String period, int limit) {
    LOGGER.info(
        "getting financial metrics for {} from {}, limit={}, period={}",
        ticker,
        endDate,
        limit,
        period);
    return this.client
        .get()
        .uri(
            "/financial-metrics/?ticker={ticker}&report_period_lte={endDate}&limit={limit}&period={period}",
            ticker,
            endDate,
            limit,
            period)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(FinancialMetrics.class)
        .financialMetrics();
  }

  public List<LineItem> searchLineItems(
      String ticker, LocalDate endDate, List<String> items, String period, int limit) {
    LOGGER.info(
        "getting line items for {} from {}, limit={}, period={}", ticker, endDate, limit, period);
    return this.client
        .post()
        .uri(
            "/financials/search/line-items?ticker={ticker}&report_period_lte={endDate}&limit={limit}&period={period}",
            ticker,
            endDate,
            limit,
            period)
        .body(new LineItemSearchRequest(List.of(ticker), items, endDate, period, limit))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(SearchLineItemResults.class)
        .lineItems();
  }

  //
  // def get_insider_trades(
  //    ticker: str,
  //    end_date: str,
  //    start_date: str | None = None,
  //    limit: int = 1000,
  // ) -> list[InsiderTrade]:
  //    """Fetch insider trades from cache or API."""
  //    # Check cache first
  //    if cached_data := _cache.get_insider_trades(ticker):
  //        # Filter cached data by date range
  //        filtered_data = [InsiderTrade(**trade) for trade in cached_data if (start_date is None
  // or (trade.get("transaction_date") or trade["filing_date"]) >= start_date) and
  // (trade.get("transaction_date") or trade["filing_date"]) <= end_date]
  //        filtered_data.sort(key=lambda x: x.transaction_date or x.filing_date, reverse=True)
  //        if filtered_data:
  //            return filtered_data
  //
  //    # If not in cache or insufficient data, fetch from API
  //    headers = {}
  //    if api_key := os.environ.get("FINANCIAL_DATASETS_API_KEY"):
  //        headers["X-API-KEY"] = api_key
  //
  //    all_trades = []
  //    current_end_date = end_date
  //
  //    while True:
  //        url =
  // f"https://api.financialdatasets.ai/insider-trades/?ticker={ticker}&filing_date_lte={current_end_date}"
  //        if start_date:
  //            url += f"&filing_date_gte={start_date}"
  //        url += f"&limit={limit}"
  //
  //        response = requests.get(url, headers=headers)
  //        if response.status_code != 200:
  //            raise Exception(f"Error fetching data: {ticker} - {response.status_code} -
  // {response.text}")
  //
  //        data = response.json()
  //        response_model = InsiderTradeResponse(**data)
  //        insider_trades = response_model.insider_trades
  //
  //        if not insider_trades:
  //            break
  //
  //        all_trades.extend(insider_trades)
  //
  //        # Only continue pagination if we have a start_date and got a full page
  //        if not start_date or len(insider_trades) < limit:
  //            break
  //
  //        # Update end_date to the oldest filing date from current batch for next iteration
  //        current_end_date = min(trade.filing_date for trade in insider_trades).split("T")[0]
  //
  //        # If we've reached or passed the start_date, we can stop
  //        if current_end_date <= start_date:
  //            break
  //
  //    if not all_trades:
  //        return []
  //
  //    # Cache the results
  //    _cache.set_insider_trades(ticker, [trade.model_dump() for trade in all_trades])
  //    return all_trades
  //
  //
  // def get_company_news(
  //    ticker: str,
  //    end_date: str,
  //    start_date: str | None = None,
  //    limit: int = 1000,
  // ) -> list[CompanyNews]:
  //    """Fetch company news from cache or API."""
  //    # Check cache first
  //    if cached_data := _cache.get_company_news(ticker):
  //        # Filter cached data by date range
  //        filtered_data = [CompanyNews(**news) for news in cached_data if (start_date is None or
  // news["date"] >= start_date) and news["date"] <= end_date]
  //        filtered_data.sort(key=lambda x: x.date, reverse=True)
  //        if filtered_data:
  //            return filtered_data
  //
  //    # If not in cache or insufficient data, fetch from API
  //    headers = {}
  //    if api_key := os.environ.get("FINANCIAL_DATASETS_API_KEY"):
  //        headers["X-API-KEY"] = api_key
  //
  //    all_news = []
  //    current_end_date = end_date
  //
  //    while True:
  //        url =
  // f"https://api.financialdatasets.ai/news/?ticker={ticker}&end_date={current_end_date}"
  //        if start_date:
  //            url += f"&start_date={start_date}"
  //        url += f"&limit={limit}"
  //
  //        response = requests.get(url, headers=headers)
  //        if response.status_code != 200:
  //            raise Exception(f"Error fetching data: {ticker} - {response.status_code} -
  // {response.text}")
  //
  //        data = response.json()
  //        response_model = CompanyNewsResponse(**data)
  //        company_news = response_model.news
  //
  //        if not company_news:
  //            break
  //
  //        all_news.extend(company_news)
  //
  //        # Only continue pagination if we have a start_date and got a full page
  //        if not start_date or len(company_news) < limit:
  //            break
  //
  //        # Update end_date to the oldest date from current batch for next iteration
  //        current_end_date = min(news.date for news in company_news).split("T")[0]
  //
  //        # If we've reached or passed the start_date, we can stop
  //        if current_end_date <= start_date:
  //            break
  //
  //    if not all_news:
  //        return []
  //
  //    # Cache the results
  //    _cache.set_company_news(ticker, [news.model_dump() for news in all_news])
  //    return all_news
  //
  public BigDecimal getMarketCap(String ticker, LocalDate endDate) {
    LOGGER.info("getting market cap for {}", ticker);

    if (endDate.isEqual(LocalDate.now())) {
      return companyFacts(ticker).marketCap();
    }

    return getFinancialMetrics(ticker, endDate, "ttm", 10).get(0).marketCap();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PricesHolder(
      @JsonProperty("prices") List<Price> prices,
      @JsonProperty("next_page_url") String nextPriceUrl) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Price(
      @JsonProperty("open") BigDecimal open,
      @JsonProperty("close") BigDecimal close,
      @JsonProperty("high") BigDecimal high,
      @JsonProperty("low") BigDecimal low,
      @JsonProperty("volume") BigDecimal volume,
      @JsonProperty("time") String time,
      @JsonProperty("time_milliseconds") BigInteger timeMillisecond) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CompanyFactsHolder(@JsonProperty("company_facts") CompanyFacts companyFacts) {}

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
      @JsonProperty("sic_code") String sicCode,
      @JsonProperty("sic_industry") String sicIndustry,
      @JsonProperty("sic_sector") String sicSector,
      @JsonProperty("website_url") String websiteUrl,
      @JsonProperty("weighted_average_shares") BigDecimal weightedAverageShares) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record FinancialMetrics(
      @JsonProperty("financial_metrics") List<Metric> financialMetrics) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Metric(
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

  public record LineItemSearchRequest(
      @JsonProperty("tickers") List<String> tickers,
      @JsonProperty("line_items") List<String> lineItems,
      @JsonProperty("end_date") LocalDate endDate,
      @JsonProperty("period") String period,
      @JsonProperty("limit") int limit) {}

  public record SearchLineItemResults(@JsonProperty("search_results") List<LineItem> lineItems) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record LineItem(
      @JsonProperty("ticker") String ticker,
      @JsonProperty("report_period") LocalDate reportPeriod,
      @JsonProperty("period") String period,
      @JsonProperty("currency") String currency) {}
}
