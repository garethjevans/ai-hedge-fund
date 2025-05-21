package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

public class FinancialDatasetsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FinancialDatasetsService.class);

  private final RestClient client;
  private final ObjectMapper mapper;
  private final boolean cacheEnabled;
  private final CacheService cacheService;

  public FinancialDatasetsService(
      RestClient.Builder builder,
      String url,
      String apiKey,
      boolean cacheEnabled,
      ObjectMapper mapper,
      CacheService cacheService) {
    this.client =
        builder
            .baseUrl(url)
            .defaultHeader("X-API-KEY", apiKey)
            .requestInterceptor(
                (request, body, execution) -> {
                  logRequest(request, body);
                  var response = execution.execute(request, body);
                  // logResponse(request, response);
                  return response;
                })
            .build();
    this.cacheEnabled = cacheEnabled;
    this.mapper = mapper;
    this.cacheService = cacheService;
  }

  private void logRequest(HttpRequest request, byte[] body) {
    LOGGER.info("Request: {} {}", request.getMethod(), request.getURI());
    // logHeaders(request.getHeaders());
    if (body != null && body.length > 0) {
      LOGGER.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
    }
  }

  private <T> T cacheAwareGet(Class<T> type, String uri, Object... uriVariables) {
    String cacheableUri = UriComponentsBuilder.fromUriString(uri).build(uriVariables).toString();
    LOGGER.info("cacheable uri: {}", cacheableUri);

    if (cacheEnabled && cacheService.keyExists(cacheableUri)) {
      try {
        T t = mapper.readValue(cacheService.get(cacheableUri), type);
        LOGGER.info("got response body from cache: {}", t);
        return t;
      } catch (JsonProcessingException e) {
        LOGGER.warn("Unable to read value from cache", e);
      }
    }

    T t =
        this.client
            .get()
            .uri(uri, uriVariables)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(type);

    LOGGER.info("got response body: {}", t);

    try {
      cacheService.save(cacheableUri, mapper.writeValueAsString(t));
    } catch (JsonProcessingException e) {
      LOGGER.warn("Unable to persist response to cache", e);
    }
    return t;
  }

  private <T> T cacheAwarePost(Class<T> type, Object body, String uri, Object... uriVariables) {
    String cacheableUri = UriComponentsBuilder.fromUriString(uri).build(uriVariables).toString();

    String jsonBody = null;
    try {
      jsonBody = mapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    LOGGER.info("cacheable uri: {}, with body {}", cacheableUri, jsonBody);
    String cacheKey = cacheableUri + "-" + jsonBody;

    if (cacheEnabled && cacheService.keyExists(cacheKey)) {
      try {
        T t = mapper.readValue(cacheService.get(cacheKey), type);
        LOGGER.info("got response body from cache: {}", t);
        return t;
      } catch (JsonProcessingException e) {
        LOGGER.warn("Unable to read value from cache", e);
      }
    }

    T t =
        this.client
            .post()
            .uri(uri, uriVariables)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(type);

    LOGGER.info("got response body: {}", t);

    try {
      cacheService.save(cacheKey, mapper.writeValueAsString(t));
    } catch (JsonProcessingException e) {
      LOGGER.warn("Unable to persist response to cache", e);
    }

    return t;
  }

  public Facts companyFacts(String ticker) {
    LOGGER.info("getting company facts for {}", ticker);

    return cacheAwareGet(CompanyFactsHolder.class, "/company/facts/?ticker={ticker}", ticker)
        .companyFacts();
  }

  //  public List<Price> getPrices(String ticker, LocalDate startDate, LocalDate endDate) {
  //    LOGGER.info("getting prices for {} between {} and {}", ticker, startDate, endDate);
  //    return cacheAwareGet(PricesHolder.class,
  // "/prices/?ticker={ticker}&interval=day&interval_multiplier=1&start_date={startDate}&end_date={endDate}",
  //            ticker,
  //            startDate,
  //            endDate).prices();
  //  }

  public List<Metrics> getFinancialMetrics(
      String ticker, LocalDate endDate, Period period, int limit) {
    return cacheAwareGet(
            FinancialMetrics.class,
            "/financial-metrics/?ticker={ticker}&report_period_lte={endDate}&limit={limit}&period={period}",
            ticker,
            endDate,
            limit,
            period)
        .financialMetrics();
  }

  public List<LineItem> searchLineItems(
      String ticker, LocalDate endDate, List<String> items, Period period, int limit) {
    return cacheAwarePost(
            SearchLineItemResults.class,
            new LineItemSearchRequest(List.of(ticker), items, period, limit),
            "/financials/search/line-items")
        .lineItems()
        .stream()
        .map(LineItem::new)
        .toList();
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
    if (endDate.isEqual(LocalDate.now())) {
      return companyFacts(ticker).marketCap();
    }

    return getFinancialMetrics(ticker, endDate, Period.ttm, 10).get(0).marketCap();
  }

  //  @JsonIgnoreProperties(ignoreUnknown = true)
  //  public record PricesHolder(
  //      @JsonProperty("prices") List<Price> prices,
  //      @JsonProperty("next_page_url") String nextPriceUrl) {}

  //  @JsonIgnoreProperties(ignoreUnknown = true)
  //  public record Price(
  //      @JsonProperty("open") BigDecimal open,
  //      @JsonProperty("close") BigDecimal close,
  //      @JsonProperty("high") BigDecimal high,
  //      @JsonProperty("low") BigDecimal low,
  //      @JsonProperty("volume") BigDecimal volume,
  //      @JsonProperty("time") String time,
  //      @JsonProperty("time_milliseconds") BigInteger timeMillisecond) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record CompanyFactsHolder(@JsonProperty("company_facts") Facts companyFacts) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record FinancialMetrics(
      @JsonProperty("financial_metrics") List<Metrics> financialMetrics) {}

  private record LineItemSearchRequest(
      @JsonProperty("tickers") List<String> tickers,
      @JsonProperty("line_items") List<String> lineItems,
      // @JsonProperty("end_date") LocalDate endDate,
      @JsonProperty("period") Period period,
      @JsonProperty("limit") int limit) {}

  private record SearchLineItemResults(
      @JsonProperty("search_results") List<Map<String, Object>> lineItems) {}
}
