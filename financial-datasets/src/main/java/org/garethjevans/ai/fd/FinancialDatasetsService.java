package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    return cacheAwareGet(CompanyFactsHolder.class, "/company/facts/?ticker={ticker}", ticker)
        .companyFacts();
  }

  public List<Price> getPrices(String ticker, LocalDate startDate, LocalDate endDate) {
    return cacheAwareGet(
            PricesResult.class,
            "/prices/?ticker={ticker}&interval=day&interval_multiplier=1&start_date={startDate}&end_date={endDate}",
            ticker,
            startDate,
            endDate)
        .prices();
  }

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

  public List<InsiderTrade> getInsiderTrades(
      String ticker, LocalDate startDate, LocalDate endDate, int limit) {

    return allPagedResultsByDateAndLimit(
        batchEndDate -> {
          return cacheAwareGet(
                  InsiderTradesResult.class,
                  "/insider-trades/?ticker={ticker}&filing_date_gte={start_date}&filing_date_lte={end_date}&limit={limit}",
                  ticker,
                  startDate,
                  batchEndDate,
                  limit)
              .insiderTrades();
        },
        InsiderTrade::filingDate,
        startDate,
        endDate,
        limit);
  }

  public List<CompanyNews> getCompanyNews(
      String ticker, LocalDate startDate, LocalDate endDate, int limit) {

    return allPagedResultsByDateAndLimit(
        batchEndDate -> {
          return cacheAwareGet(
                  CompanyNewsResult.class,
                  "/news/?ticker={ticker}&start_date={start_date}&end_date={end_date}&limit={limit}",
                  ticker,
                  startDate,
                  batchEndDate,
                  limit)
              .companyNews();
        },
        CompanyNews::date,
        startDate,
        endDate,
        limit);
  }

  public BigDecimal getMarketCap(String ticker, LocalDate endDate) {
    if (endDate.isEqual(LocalDate.now())) {
      return companyFacts(ticker).marketCap();
    }

    return getFinancialMetrics(ticker, endDate, Period.ttm, 10).get(0).marketCap();
  }

  private <T> List<T> allPagedResultsByDateAndLimit(
      Function<LocalDate, List<T>> get,
      Function<T, LocalDate> extractNewDate,
      LocalDate startDate,
      LocalDate endDate,
      int limit) {
    List<T> all = new ArrayList<>();
    boolean more = true;
    LocalDate batchEndDate = endDate;

    while (more) {
      List<T> batch = get.apply(batchEndDate);

      all.addAll(batch);

      if (batch.size() < limit) {
        more = false;
      }

      // Update end_date to the oldest date from current batch for next iteration
      batchEndDate = batch.stream().map(extractNewDate).min(LocalDate::compareTo).get();

      // If we've reached or passed the start_date, we can stop
      if (batchEndDate.isBefore(startDate)) {
        more = false;
      }
    }

    return all;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record PricesResult(
      @JsonProperty("prices") List<Price> prices,
      @JsonProperty("next_page_url") String nextPriceUrl) {}

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

  private record InsiderTradesResult(
      @JsonProperty("insider_trades") List<InsiderTrade> insiderTrades) {}

  private record CompanyNewsResult(@JsonProperty("news") List<CompanyNews> companyNews) {}
}
