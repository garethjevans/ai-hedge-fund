package org.garethjevans.ai.agent.peterlynch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.garethjevans.ai.common.AgentSignal;
import org.garethjevans.ai.common.Result;
import org.garethjevans.ai.common.Signal;
import org.garethjevans.ai.fd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentPeterLynchTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentPeterLynchTool.class);

  private static final String AGENT_NAME = "Peter Lynch Agent";

  private final FinancialDatasetsService financialDatasets;
  private final ObjectMapper objectMapper;

  public AgentPeterLynchTool(
      FinancialDatasetsService financialDatasets, ObjectMapper objectMapper) {
    this.financialDatasets = financialDatasets;
    this.objectMapper = objectMapper;
  }

  /**
   * Analyzes stocks using Peter Lynch's investing principles: - Invest in what you know (clear,
   * understandable businesses). - Growth at a Reasonable Price (GARP), emphasizing the PEG ratio. -
   * Look for consistent revenue & EPS increases and manageable debt. - Be alert for potential
   * "ten-baggers" (high-growth opportunities). - Avoid overly complex or highly leveraged
   * businesses. - Use news sentiment and insider trades for secondary inputs. - If fundamentals
   * strongly align with GARP, be more aggressive.
   *
   * <p>The result is a bullish/bearish/neutral signal, along with a confidence (0–100) and a
   * textual reasoning explanation.
   *
   * @param ticker
   * @param toolContext
   * @return
   */
  @Tool(
      name = "peter_lynch_analysis",
      description = "Performs stock analysis using PeterLynch's methods by ticker")
  public Map<String, AgentSignal> performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Peter Lynch's principles and LLM reasoning.");

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusMonths(3);

    List<String> tickers = List.of(ticker);

    Map<String, AgentSignal> analysis = new HashMap<>();

    for (String t : tickers) {
      updateProgress(t, "Fetching financial metrics");
      List<Metrics> metrics =
          financialDatasets.getFinancialMetrics(ticker, endDate, Period.annual, 5);

      updateProgress(t, "Gathering financial line items");
      List<LineItem> financialLineItems =
          financialDatasets.searchLineItems(
              ticker,
              endDate,
              List.of(
                  "revenue",
                  "earnings_per_share",
                  "net_income",
                  "operating_income",
                  "gross_margin",
                  "operating_margin",
                  "free_cash_flow",
                  "capital_expenditure",
                  "cash_and_equivalents",
                  "total_debt",
                  "shareholders_equity",
                  "outstanding_shares"),
              Period.annual,
              5);

      updateProgress(t, "Getting market cap");
      var marketCap = financialDatasets.getMarketCap(ticker, endDate);
      LOGGER.info("Got market cap: {}", marketCap);

      updateProgress(t, "Fetching insider trades");
      var insiderTrades = financialDatasets.getInsiderTrades(ticker, startDate, endDate, 50);
      LOGGER.info("Got insider trades: {}", insiderTrades);

      updateProgress(t, "Fetching company news");
      var companyNews = financialDatasets.getCompanyNews(ticker, startDate, endDate, 50);
      LOGGER.info("Got company news: {}", companyNews);

      updateProgress(t, "Fetching recent price data for reference");
      var prices = financialDatasets.getPrices(ticker, startDate, endDate);
      LOGGER.info("Got prices: {}", prices);

      // Perform sub-analyses:
      updateProgress(t, "Analyzing growth");
      var growthAnalysis = analyzeLynchGrowth(financialLineItems);

      updateProgress(t, "Analyzing fundamentals");
      var fundamentalsAnalysis = analyzeLynchFundamentals(financialLineItems);

      updateProgress(t, "Analyzing valuation (focus on PEG)");
      var valuationAnalysis = analyzeLynchValuation(financialLineItems, marketCap);

      updateProgress(t, "Analyzing sentiment");
      var sentimentAnalysis = analyzeSentiment(companyNews);

      updateProgress(t, "Analyzing insider activity");
      var insiderActivity = analyzeInsiderActivity(insiderTrades);

      // Combine partial scores with weights typical for Peter Lynch:
      // 30% Growth, 25% Valuation, 20% Fundamentals,
      // 15% Sentiment, 10% Insider Activity = 100%
      double totalScore =
          new BigDecimal(growthAnalysis.score())
              .multiply(new BigDecimal("0.30"))
              .add(new BigDecimal(valuationAnalysis.score()).multiply(new BigDecimal("0.25")))
              .add(new BigDecimal(fundamentalsAnalysis.score()).multiply(new BigDecimal("0.20")))
              .add(new BigDecimal(sentimentAnalysis.score()).multiply(new BigDecimal("0.15")))
              .add(new BigDecimal(insiderActivity.score()).multiply(new BigDecimal("0.10")))
              .doubleValue();

      int maxPossibleScore = 10;

      Signal signal = null;
      // Map final score to signal
      if (totalScore >= 7.5) {
        signal = Signal.bullish;
      } else if (totalScore <= 4.5) {
        signal = Signal.bearish;
      } else {
        signal = Signal.neutral;
      }

      updateProgress(t, "Generating Peter Lynch analysis");

      AnalysisResult analysisResult =
          new AnalysisResult(
              signal,
              (int) totalScore,
              maxPossibleScore,
              growthAnalysis,
              valuationAnalysis,
              fundamentalsAnalysis,
              sentimentAnalysis,
              insiderActivity);

      updateProgress(t, "Generating Peter Lynch analysis");

      AgentSignal output = generateOutput(t, analysisResult, toolContext);

      // Store analysis in consistent format with other agents
      analysis.put(ticker, output);

      updateProgress(t, "Done");

      updateProgress(null, "Done");
    }

    return analysis;
  }

  /**
   * Evaluate growth based on revenue and EPS trends:
   *
   * <ul>
   *   <li>Consistent revenue growth
   *   <li>Consistent EPS growth
   * </ul>
   *
   * Peter Lynch liked companies with steady, understandable growth, often searching for potential
   * 'ten-baggers' with a long runway.
   */
  public Result analyzeLynchGrowth(List<LineItem> lineItems) {
    if (lineItems == null || lineItems.size() < 2) {
      return new Result(0, 0, "Insufficient financial data for growth analysis");
    }

    List<String> details = new ArrayList<>();
    int rawScore = 0;

    // 1) Revenue Growth
    var revenues =
        lineItems.stream()
            .filter(l -> l.get("revenue") != null)
            .map(l -> l.get("revenue"))
            .toList();

    // revenues = [fi.revenue for fi in financial_line_items if fi.revenue is not None]
    if (revenues.size() >= 2) {
      BigDecimal latestRev = revenues.getFirst();
      BigDecimal oldestRev = revenues.getLast();

      if (oldestRev.compareTo(BigDecimal.ZERO) > 0) {
        // rev_growth = (latest_rev - older_rev) / abs(older_rev)
        BigDecimal revGrowth =
            (latestRev.subtract(oldestRev)).divide(oldestRev.abs(), 2, RoundingMode.HALF_UP);

        if (revGrowth.compareTo(new BigDecimal("0.25")) > 0) {
          rawScore += 3;
          details.add("Strong revenue growth: " + revGrowth);
        } else if (revGrowth.compareTo(new BigDecimal("0.10")) > 0) {
          rawScore += 2;
          details.add("Moderate revenue growth: {rev_growth:.1%}");
        } else if (revGrowth.compareTo(new BigDecimal("0.02")) > 0) {
          rawScore += 1;
          details.add("Slight revenue growth: {rev_growth:.1%}");
        } else {
          details.add("Flat or negative revenue growth: {rev_growth:.1%}");
        }
      } else {
        details.add("Older revenue is zero/negative; can't compute revenue growth.");
      }
    } else {
      details.add("Not enough revenue data to assess growth.");
    }

    // 2) EPS Growth
    var epsValues =
        lineItems.stream()
            .filter(l -> l.get("earnings_per_share") != null)
            .map(l -> l.get("earnings_per_share"))
            .toList();

    if (epsValues.size() >= 2) {
      BigDecimal latestEps = epsValues.getFirst();
      BigDecimal oldestEps = epsValues.getLast();

      //        if abs(older_eps) > 1e-9:
      if (oldestEps.abs().compareTo(new BigDecimal("1e-9")) > 0) {
        BigDecimal epsGrowth =
            (latestEps.subtract(oldestEps)).divide(oldestEps.abs(), 2, RoundingMode.HALF_UP);
        if (epsGrowth.compareTo(new BigDecimal("0.25")) > 0) {
          rawScore += 3;
          details.add("Strong EPS growth: " + epsGrowth);
        } else if (epsGrowth.compareTo(new BigDecimal("0.10")) > 0) {
          rawScore += 2;
          details.add("Moderate EPS growth: " + epsGrowth);
        } else if (epsGrowth.compareTo(new BigDecimal("0.02")) > 0) {
          rawScore += 1;
          details.add("Slight EPS growth: " + epsGrowth);
        } else {
          details.add("Minimal or negative EPS growth: " + epsGrowth);
        }
      } else {
        details.add("Older EPS is near zero; skipping EPS growth calculation.");
      }
    } else {
      details.add("Not enough EPS data for growth calculation.");
    }

    int finalScore = Math.min(10, (rawScore / 6) * 10);
    return new Result(finalScore, 0, String.join("; ", details));
  }

  /**
   * Evaluate basic fundamentals:
   *
   * <ul>
   *   <li>Debt/Equity
   *   <li>Operating margin (or gross margin)
   *   <li>Positive Free Cash Flow
   * </ul>
   *
   * Lynch avoided heavily indebted or complicated businesses.
   */
  public Result analyzeLynchFundamentals(List<LineItem> lineItems) {
    if (lineItems == null || lineItems.isEmpty()) {
      return new Result(0, 0, "Insufficient fundamentals data");
    }

    List<String> details = new ArrayList<>();
    int rawScore = 0;

    // 1) Debt-to-Equity

    List<BigDecimal> debtValues =
        lineItems.stream()
            .filter(l -> l.get("total_debt") != null)
            .map(l -> l.get("total_debt"))
            .toList();
    List<BigDecimal> equityValues =
        lineItems.stream()
            .filter(l -> l.get("shareholders_equity") != null)
            .map(l -> l.get("shareholders_equity"))
            .toList();

    // if debt_values and eq_values and len(debt_values) == len(eq_values) and len(debt_values) > 0:
    //        recent_debt = debt_values[0]
    //        recent_equity = eq_values[0] if eq_values[0] else 1e-9
    //        de_ratio = recent_debt / recent_equity
    //        if de_ratio < 0.5:
    //            raw_score += 2
    //            details.append(f"Low debt-to-equity: {de_ratio:.2f}")
    //        elif de_ratio < 1.0:
    //            raw_score += 1
    //            details.append(f"Moderate debt-to-equity: {de_ratio:.2f}")
    //        else:
    //            details.append(f"High debt-to-equity: {de_ratio:.2f}")
    //    else:
    //        details.append("No consistent debt/equity data available.")
    //
    // 2) Operating Margin
    List<BigDecimal> operatingMarginValues =
        lineItems.stream()
            .filter(l -> l.get("operating_margin") != null)
            .map(l -> l.get("operating_margin"))
            .toList();
    //    if om_values:
    //        om_recent = om_values[0]
    //        if om_recent > 0.20:
    //            raw_score += 2
    //            details.append(f"Strong operating margin: {om_recent:.1%}")
    //        elif om_recent > 0.10:
    //            raw_score += 1
    //            details.append(f"Moderate operating margin: {om_recent:.1%}")
    //        else:
    //            details.append(f"Low operating margin: {om_recent:.1%}")
    //    else:
    //        details.append("No operating margin data available.")

    // 3) Positive Free Cash Flow
    List<BigDecimal> freeCashFlowValues =
        lineItems.stream()
            .filter(l -> l.get("free_cash_flow") != null)
            .map(l -> l.get("free_cash_flow"))
            .toList();

    //    if fcf_values and fcf_values[0] is not None:
    //        if fcf_values[0] > 0:
    //            raw_score += 2
    //            details.append(f"Positive free cash flow: {fcf_values[0]:,.0f}")
    //        else:
    //            details.append(f"Recent FCF is negative: {fcf_values[0]:,.0f}")
    //    else:
    //        details.append("No free cash flow data available.")
    //
    //    # raw_score up to 6 => scale to 0–10
    //    final_score = min(10, (raw_score / 6) * 10)
    //    return {"score": final_score, "details": "; ".join(details)}
    return new Result(rawScore, 0, String.join("; ", details));
  }

  public Result analyzeLynchValuation(List<LineItem> lineItems, BigDecimal marketCap) {
    // def analyze_lynch_valuation(financial_line_items: list, market_cap: float | None) -> dict:
    //    """
    //    Peter Lynch's approach to 'Growth at a Reasonable Price' (GARP):
    //      - Emphasize the PEG ratio: (P/E) / Growth Rate
    //      - Also consider a basic P/E if PEG is unavailable
    //    A PEG < 1 is very attractive; 1-2 is fair; >2 is expensive.
    //    """
    //    if not financial_line_items or market_cap is None:
    //        return {"score": 0, "details": "Insufficient data for valuation"}
    //
    List<String> details = new ArrayList<>();
    int rawScore = 0;
    //
    //    # Gather data for P/E
    //    net_incomes = [fi.net_income for fi in financial_line_items if fi.net_income is not
    // None]
    //    eps_values = [fi.earnings_per_share for fi in financial_line_items if
    // fi.earnings_per_share is not None]
    //
    //    # Approximate P/E via (market cap / net income) if net income is positive
    //    pe_ratio = None
    //    if net_incomes and net_incomes[0] and net_incomes[0] > 0:
    //        pe_ratio = market_cap / net_incomes[0]
    //        details.append(f"Estimated P/E: {pe_ratio:.2f}")
    //    else:
    //        details.append("No positive net income => can't compute approximate P/E")
    //
    //    # If we have at least 2 EPS data points, let's estimate growth
    //    eps_growth_rate = None
    //    if len(eps_values) >= 2:
    //        latest_eps = eps_values[0]
    //        older_eps = eps_values[-1]
    //        if older_eps > 0:
    //            eps_growth_rate = (latest_eps - older_eps) / older_eps
    //            details.append(f"Approx EPS growth rate: {eps_growth_rate:.1%}")
    //        else:
    //            details.append("Cannot compute EPS growth rate (older EPS <= 0)")
    //    else:
    //        details.append("Not enough EPS data to compute growth rate")
    //
    //    # Compute PEG if possible
    //    peg_ratio = None
    //    if pe_ratio and eps_growth_rate and eps_growth_rate > 0:
    //        # Peg ratio typically uses a percentage growth rate
    //        # So if growth rate is 0.25, we treat it as 25 for the formula => PE / 25
    //        # Alternatively, some treat it as 0.25 => we do (PE / (0.25 * 100)).
    //        # Implementation can vary, but let's do a standard approach: PEG = PE / (Growth *
    // 100).
    //        peg_ratio = pe_ratio / (eps_growth_rate * 100)
    //        details.append(f"PEG ratio: {peg_ratio:.2f}")
    //
    //    # Scoring logic:
    //    #   - P/E < 15 => +2, < 25 => +1
    //    #   - PEG < 1 => +3, < 2 => +2, < 3 => +1
    //    if pe_ratio is not None:
    //        if pe_ratio < 15:
    //            raw_score += 2
    //        elif pe_ratio < 25:
    //            raw_score += 1
    //
    //    if peg_ratio is not None:
    //        if peg_ratio < 1:
    //            raw_score += 3
    //        elif peg_ratio < 2:
    //            raw_score += 2
    //        elif peg_ratio < 3:
    //            raw_score += 1
    //
    //    final_score = min(10, (raw_score / 5) * 10)
    //    return {"score": final_score, "details": "; ".join(details)}
    return new Result(rawScore, 0, String.join("; ", details));
  }

  public Result analyzeSentiment(List<CompanyNews> news) {
    // def analyze_sentiment(news_items: list) -> dict:
    //    """
    //    Basic news sentiment check. Negative headlines weigh on the final score.
    //    """
    //    if not news_items:
    //        return {"score": 5, "details": "No news data; default to neutral sentiment"}
    //
    //    negative_keywords = ["lawsuit", "fraud", "negative", "downturn", "decline",
    // "investigation", "recall"]
    //    negative_count = 0
    //    for news in news_items:
    //        title_lower = (news.title or "").lower()
    //        if any(word in title_lower for word in negative_keywords):
    //            negative_count += 1
    //
    List<String> details = new ArrayList<>();
    int rawScore = 0;
    //    if negative_count > len(news_items) * 0.3:
    //        # More than 30% negative => somewhat bearish => 3/10
    //        score = 3
    //        details.append(f"High proportion of negative headlines:
    // {negative_count}/{len(news_items)}")
    //    elif negative_count > 0:
    //        # Some negativity => 6/10
    //        score = 6
    //        details.append(f"Some negative headlines: {negative_count}/{len(news_items)}")
    //    else:
    //        # Mostly positive => 8/10
    //        score = 8
    //        details.append("Mostly positive or neutral headlines")
    //
    //    return {"score": score, "details": "; ".join(details)}
    return new Result(rawScore, 0, String.join("; ", details));
  }

  /**
   * Simple insider-trade analysis:
   *
   * <ul>
   *   <li>If there's heavy insider buying, it's a positive sign.
   *   <li>If there's mostly selling, it's a negative sign.
   *   <li>Otherwise, neutral.
   * </ul>
   */
  public Result analyzeInsiderActivity(List<InsiderTrade> insiderTrades) {

    // Default 5 (neutral)
    int score = 5;
    List<String> details = new ArrayList<>();
    //
    //    if not insider_trades:
    //        details.append("No insider trades data; defaulting to neutral")
    //        return {"score": score, "details": "; ".join(details)}
    //
    //    buys, sells = 0, 0
    //    for trade in insider_trades:
    //        if trade.transaction_shares is not None:
    //            if trade.transaction_shares > 0:
    //                buys += 1
    //            elif trade.transaction_shares < 0:
    //                sells += 1
    //
    //    total = buys + sells
    //    if total == 0:
    //        details.append("No significant buy/sell transactions found; neutral stance")
    //        return {"score": score, "details": "; ".join(details)}
    //
    //    buy_ratio = buys / total
    //    if buy_ratio > 0.7:
    //        # Heavy buying => +3 => total 8
    //        score = 8
    //        details.append(f"Heavy insider buying: {buys} buys vs. {sells} sells")
    //    elif buy_ratio > 0.4:
    //        # Some buying => +1 => total 6
    //        score = 6
    //        details.append(f"Moderate insider buying: {buys} buys vs. {sells} sells")
    //    else:
    //        # Mostly selling => -1 => total 4
    //        score = 4
    //        details.append(f"Mostly insider selling: {buys} buys vs. {sells} sells")
    //
    //    return {"score": score, "details": "; ".join(details)}
    return new Result(score, 0, String.join("; ", details));
  }

  private AgentSignal generateOutput(
      String ticker, AnalysisResult analysisResult, ToolContext toolContext) {
    StringBuilder output = new StringBuilder();

    // LOGGER.info("toolContext: {}", toolContext.getContext());
    McpToolUtils.getMcpExchange(toolContext)
        .ifPresent(
            exchange -> {
              exchange.loggingNotification(
                  McpSchema.LoggingMessageNotification.builder()
                      .level(McpSchema.LoggingLevel.INFO)
                      .data("Start sampling")
                      .build());

              if (exchange.getClientCapabilities().sampling() != null) {
                var messageRequestBuilder =
                    McpSchema.CreateMessageRequest.builder()
                        .systemPrompt(generateSystemMessage())
                        .messages(
                            List.of(
                                new McpSchema.SamplingMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent(
                                        generateUserMessage(ticker, analysisResult)))));

                var llmMessageRequest =
                    messageRequestBuilder
                        .modelPreferences(
                            McpSchema.ModelPreferences.builder().addHint("gpt-4o").build())
                        .build();
                McpSchema.CreateMessageResult llmResponse =
                    exchange.createMessage(llmMessageRequest);

                output.append(((McpSchema.TextContent) llmResponse.content()).text());
              }

              exchange.loggingNotification(
                  McpSchema.LoggingMessageNotification.builder()
                      .level(McpSchema.LoggingLevel.INFO)
                      .data("Finish Sampling")
                      .build());
            });

    String withoutMarkdown = removeMarkdown(output.toString());
    LOGGER.info("Got sampling response '{}'", withoutMarkdown);

    try {
      return objectMapper.readValue(withoutMarkdown, AgentSignal.class);
    } catch (JsonProcessingException e) {
      LOGGER.warn("Error in analysis, defaulting to neutral", e);
      return new AgentSignal(Signal.neutral, 0f, "Error in analysis, defaulting to neutral");
    }
  }

  private String removeMarkdown(String in) {
    return in.replace("```json", "").replace("```", "").trim();
  }

  public String generateSystemMessage() {
    String body =
        """
            You are a Peter Lynch AI agent. You make investment decisions based on Peter Lynch's well-known principles:

            1. Invest in What You Know: Emphasize understandable businesses, possibly discovered in everyday life.
            2. Growth at a Reasonable Price (GARP): Rely on the PEG ratio as a prime metric.
            3. Look for 'Ten-Baggers': Companies capable of growing earnings and share price substantially.
            4. Steady Growth: Prefer consistent revenue/earnings expansion, less concern about short-term noise.
            5. Avoid High Debt: Watch for dangerous leverage.
            6. Management & Story: A good 'story' behind the stock, but not overhyped or too complex.

            When you provide your reasoning, do it in Peter Lynch's voice:
            - Cite the PEG ratio
            - Mention 'ten-bagger' potential if applicable
            - Refer to personal or anecdotal observations (e.g., "If my kids love the product...")
            - Use practical, folksy language
            - Provide key positives and negatives
            - Conclude with a clear stance (bullish, bearish, or neutral)
            """;
    LOGGER.info(body);
    return body;
  }

  public String generateUserMessage(String ticker, AnalysisResult analysisData) {
    PromptTemplate promptTemplate =
        PromptTemplate.builder()
            .renderer(
                StTemplateRenderer.builder()
                    .startDelimiterToken('{')
                    .endDelimiterToken('}')
                    .build())
            .template(
                """
                                            Based on the following analysis data for {ticker}, produce your Peter Lynch–style investment signal.

                                            Analysis Data for {ticker}:
                                            {analysis_data}

                                            Return the trading signal in the following JSON format exactly:
                                            {{
                                              "signal": "bullish" | "bearish" | "neutral",
                                              "confidence": float between 0 and 100,
                                              "reasoning": "string"
                                            }}
                                           """)
            .build();

    String analysisDataJson = null;

    try {
      analysisDataJson = objectMapper.writeValueAsString(analysisData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    String body =
        promptTemplate.render(Map.of("ticker", ticker, "analysis_data", analysisDataJson));
    LOGGER.info(body);
    return body;
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: {} - {}", AGENT_NAME, ticker, message);
  }

  public record AnalysisResult(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("score") int score,
      @JsonProperty("max_score") int maxScore,
      @JsonProperty("growth_analysis") Result growthAnalysis,
      @JsonProperty("valuation_analysis") Result valuationAnalysis,
      @JsonProperty("fundamentals_analysis") Result fundamentalsAnalysis,
      @JsonProperty("sentiment_analysis") Result sentimentAnalysis,
      @JsonProperty("insider_activity") Result insiderActivity) {}
}
