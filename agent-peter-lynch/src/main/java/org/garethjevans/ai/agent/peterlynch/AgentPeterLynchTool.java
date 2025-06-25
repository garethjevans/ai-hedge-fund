package org.garethjevans.ai.agent.peterlynch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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
  public AgentSignal performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Peter Lynch's principles and LLM reasoning.");

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusMonths(3);

    //    updateProgress(ticker, "Fetching financial metrics");
    //    List<Metrics> metrics =
    //        financialDatasets.getFinancialMetrics(ticker, endDate, Period.annual, 5);

    updateProgress(ticker, "Gathering financial line items");
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

    updateProgress(ticker, "Getting market cap");
    var marketCap = financialDatasets.getMarketCap(ticker, endDate);
    LOGGER.info("Got market cap: {}", marketCap);

    updateProgress(ticker, "Fetching insider trades");
    var insiderTrades = financialDatasets.getInsiderTrades(ticker, startDate, endDate, 50);
    LOGGER.info("Got insider trades: {}", insiderTrades);

    updateProgress(ticker, "Fetching company news");
    var companyNews = financialDatasets.getCompanyNews(ticker, startDate, endDate, 50);
    LOGGER.info("Got company news: {}", companyNews);

    updateProgress(ticker, "Fetching recent price data for reference");
    var prices = financialDatasets.getPrices(ticker, startDate, endDate);
    LOGGER.info("Got prices: {}", prices);

    // Perform sub-analyses:
    updateProgress(ticker, "Analyzing growth");
    var growthAnalysis = analyzeLynchGrowth(financialLineItems);

    updateProgress(ticker, "Analyzing fundamentals");
    var fundamentalsAnalysis = analyzeLynchFundamentals(financialLineItems);

    updateProgress(ticker, "Analyzing valuation (focus on PEG)");
    var valuationAnalysis = analyzeLynchValuation(financialLineItems, marketCap);

    updateProgress(ticker, "Analyzing sentiment");
    var sentimentAnalysis = analyzeSentiment(companyNews);

    updateProgress(ticker, "Analyzing insider activity");
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

    updateProgress(ticker, "Generating Peter Lynch analysis");

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

    updateProgress(ticker, "Generating Peter Lynch analysis");

    AgentSignal output = generateOutput(ticker, analysisResult, toolContext);

    updateProgress(ticker, "Done");

    return output;
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

    if (debtValues.size() == equityValues.size() && !debtValues.isEmpty()) {
      BigDecimal recentDebt = debtValues.get(0);
      BigDecimal recentEquity =
          equityValues.get(0).compareTo(BigDecimal.ZERO) == 0
              ? new BigDecimal("1e-9")
              : equityValues.get(0);
      BigDecimal deRatio = recentDebt.divide(recentEquity, 2, RoundingMode.HALF_UP);

      if (deRatio.compareTo(new BigDecimal("0.5")) < 0) {
        rawScore += 2;
        details.add("Low debt-to-equity: " + deRatio);
      } else if (deRatio.compareTo(new BigDecimal("1.0")) < 0) {
        rawScore += 1;
        details.add("Moderate debt-to-equity: " + deRatio);
      } else {
        details.add("High debt-to-equity: " + deRatio);
      }
    } else {
      details.add("No consistent debt/equity data available.");
    }

    // 2) Operating Margin
    List<BigDecimal> operatingMarginValues =
        lineItems.stream()
            .filter(l -> l.get("operating_margin") != null)
            .map(l -> l.get("operating_margin"))
            .toList();

    if (!operatingMarginValues.isEmpty()) {
      BigDecimal operatingMarginRecent = operatingMarginValues.get(0);
      if (operatingMarginRecent.compareTo(new BigDecimal("0.20")) > 0) {
        rawScore += 2;
        details.add("Strong operating margin: " + operatingMarginRecent);
      } else if (operatingMarginRecent.compareTo(new BigDecimal("0.10")) > 0) {
        rawScore += 1;
        details.add("Moderate operating margin: " + operatingMarginRecent);
      } else {
        details.add("Low operating margin: " + operatingMarginRecent);
      }
    } else {
      details.add("No operating margin data available.");
    }

    // 3) Positive Free Cash Flow
    List<BigDecimal> freeCashFlowValues =
        lineItems.stream()
            .filter(l -> l.get("free_cash_flow") != null)
            .map(l -> l.get("free_cash_flow"))
            .toList();

    if (!freeCashFlowValues.isEmpty()) {
      if (freeCashFlowValues.get(0).compareTo(BigDecimal.ZERO) > 0) {
        rawScore += 2;
        details.add("Positive free cash flow: " + freeCashFlowValues.get(0));
      } else {
        details.add("Recent FCF is negative: " + freeCashFlowValues.get(0));
      }
    } else {
      details.add("No free cash flow data available.");
    }

    int finalScore = Math.min(10, (rawScore / 6) * 10);
    return new Result(finalScore, 0, String.join("; ", details));
  }

  /**
   * Peter Lynch's approach to 'Growth at a Reasonable Price' (GARP):
   *
   * <ul>
   *   <li>Emphasize the PEG ratio: (P/E) / Growth Rate
   *   <li>Also consider a basic P/E if PEG is unavailable
   * </ul>
   *
   * A PEG < 1 is very attractive; 1-2 is fair; >2 is expensive.
   */
  public Result analyzeLynchValuation(List<LineItem> lineItems, BigDecimal marketCap) {
    if (lineItems.isEmpty() || marketCap == null) {
      return new Result(0, 0, "Insufficient data for valuation");
    }

    List<String> details = new ArrayList<>();
    int rawScore = 0;
    //
    // Gather data for P/E
    List<BigDecimal> netIncomes =
        lineItems.stream()
            .filter(l -> l.get("net_income") != null)
            .map(l -> l.get("net_income"))
            .toList();

    List<BigDecimal> epsValues =
        lineItems.stream()
            .filter(l -> l.get("earnings_per_share") != null)
            .map(l -> l.get("earnings_per_share"))
            .toList();

    // Approximate P/E via (market cap / net income) if net income is positive
    BigDecimal peRatio = null;
    if (!netIncomes.isEmpty() && netIncomes.get(0).compareTo(BigDecimal.ZERO) > 0) {
      peRatio = marketCap.divide(netIncomes.get(0), 2, RoundingMode.HALF_UP);
      details.add("Estimated P/E: " + peRatio);
    } else {
      details.add("No positive net income => can't compute approximate P/E");
    }

    // If we have at least 2 EPS data points, let's estimate growth
    BigDecimal epsGrowthRate = null;
    if (epsValues.size() >= 2) {
      BigDecimal latestEps = epsValues.getFirst();
      BigDecimal olderEps = epsValues.getLast();
      if (olderEps.compareTo(BigDecimal.ZERO) > 0) {
        epsGrowthRate = (latestEps.subtract(olderEps)).divide(olderEps, 2, RoundingMode.HALF_UP);
        details.add("Approx EPS growth rate: " + epsGrowthRate);
      } else {
        details.add("Cannot compute EPS growth rate (older EPS <= 0)");
      }
    } else {
      details.add("Not enough EPS data to compute growth rate");
    }

    // Compute PEG if possible
    BigDecimal pegRatio = null;
    if (peRatio != null && epsGrowthRate != null && epsGrowthRate.compareTo(BigDecimal.ZERO) > 0) {

      // Peg ratio typically uses a percentage growth rate
      // So if growth rate is 0.25, we treat it as 25 for the formula => PE / 25
      // Alternatively, some treat it as 0.25 => we do (PE / (0.25 * 100)).
      // Implementation can vary, but let's do a standard approach: PEG = PE / (Growth * 100).
      pegRatio =
          peRatio.divide(epsGrowthRate.multiply(new BigDecimal(100)), 2, RoundingMode.HALF_UP);

      details.add("PEG ratio: " + pegRatio);
    }

    // Scoring logic:
    //  - P/E < 15 => +2, < 25 => +1
    if (peRatio != null) {
      if (peRatio.compareTo(new BigDecimal(15)) < 0) {
        rawScore += 2;
      } else if (peRatio.compareTo(new BigDecimal(25)) < 0) {
        rawScore += 1;
      }
    }

    //  - PEG < 1 => +3, < 2 => +2, < 3 => +1
    if (pegRatio != null) {
      if (pegRatio.compareTo(new BigDecimal(1)) < 0) {
        rawScore += 3;
      } else if (pegRatio.compareTo(new BigDecimal(2)) < 0) {
        rawScore += 2;
      } else if (pegRatio.compareTo(new BigDecimal(3)) < 0) {
        rawScore += 1;
      }
    }

    int finalScore = Math.min(10, (rawScore / 5) * 10);
    return new Result(finalScore, 0, String.join("; ", details));
  }

  /** Basic news sentiment check. Negative headlines weigh on the final score. */
  public Result analyzeSentiment(List<CompanyNews> news) {
    if (news.isEmpty()) {
      return new Result(5, 0, "No news data; default to neutral sentiment");
    }

    List<String> negativeKeywords =
        List.of("lawsuit", "fraud", "negative", "downturn", "decline", "investigation", "recall");

    int negativeCount =
        news.stream()
            .map(n -> n.title() == null ? "" : n.title().toLowerCase())
            .map(t -> stringContainsItemFromList(t, negativeKeywords) ? 1 : 0)
            .reduce(0, Integer::sum);

    List<String> details = new ArrayList<>();
    int rawScore = 0;

    // More than 30% negative => somewhat bearish => 3/10
    if (negativeCount > news.size() * 0.3) {
      rawScore = 3;
      details.add("High proportion of negative headlines: " + negativeCount + "/" + news.size());
    } else if (negativeCount > 0) {
      // Some negativity => 6/10
      rawScore = 6;
      details.add("Some negative headlines: " + negativeCount + "/" + news.size());
    } else {
      // Mostly positive => 8/10
      rawScore = 8;
      details.add("Mostly positive or neutral headlines");
    }

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

    if (insiderTrades.isEmpty()) {
      details.add("No insider trades data; defaulting to neutral");
      return new Result(score, 0, String.join("; ", details));
    }

    long buys =
        insiderTrades.stream()
            .filter(i -> i.transactionShares() != null)
            .filter(i -> i.transactionShares().compareTo(BigDecimal.ZERO) > 0)
            .count();

    long sells =
        insiderTrades.stream()
            .filter(i -> i.transactionShares() != null)
            .filter(i -> i.transactionShares().compareTo(BigDecimal.ZERO) < 0)
            .count();

    long total = buys + sells;
    if (total == 0) {
      details.add("No significant buy/sell transactions found; neutral stance");
      return new Result(score, 0, String.join("; ", details));
    }

    BigDecimal buyRatio =
        new BigDecimal(buys).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);

    if (buyRatio.compareTo(new BigDecimal("0.7")) > 0) {
      // Heavy buying => +3 => total 8
      score = 8;
      details.add("Heavy insider buying: " + buys + " buys vs. " + sells + " sells");
    } else if (buyRatio.compareTo(new BigDecimal("0.4")) > 0) {
      // Some buying => +1 => total 6
      score = 6;
      details.add("Moderate insider buying: " + buys + " buys vs. " + sells + " sells");
    } else {
      // Mostly selling => -1 => total 4
      score = 4;
      details.add("Mostly insider selling: " + buys + " buys vs. " + sells + " sells");
    }

    return new Result(score, 0, String.join("; ", details));
  }

  private AgentSignal generateOutput(
      String ticker, AnalysisResult analysisResult, ToolContext toolContext) {
    try {
      StringBuilder samplingOutput = new StringBuilder();

      McpToolUtils.getMcpExchange(toolContext)
          .ifPresent(
              exchange -> {
                exchange.loggingNotification(
                    McpSchema.LoggingMessageNotification.builder()
                        .level(McpSchema.LoggingLevel.INFO)
                        .data("Generating " + AGENT_NAME + " output for " + ticker)
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
                          // .modelPreferences(
                          // McpSchema.ModelPreferences.builder().addHint("gpt-4o").build())
                          .build();
                  McpSchema.CreateMessageResult llmResponse =
                      exchange.createMessage(llmMessageRequest);

                  samplingOutput.append(((McpSchema.TextContent) llmResponse.content()).text());
                } else {
                  LOGGER.warn("Client does not support sampling");
                }

                exchange.loggingNotification(
                    McpSchema.LoggingMessageNotification.builder()
                        .level(McpSchema.LoggingLevel.INFO)
                        .data(AGENT_NAME + " Finished for " + ticker)
                        .build());
              });

      String withoutMarkdown = removeMarkdown(samplingOutput.toString());
      LOGGER.info("Got sampling response '{}'", withoutMarkdown);

      return objectMapper.readValue(withoutMarkdown, AgentSignal.class).withAgent(AGENT_NAME);
    } catch (Exception e) {
      LOGGER.warn("Error in analysis, defaulting to neutral", e);
      return new AgentSignal(
          AGENT_NAME, ticker, Signal.neutral, 0f, "Error in analysis, defaulting to neutral");
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
                    .startDelimiterToken('<')
                    .endDelimiterToken('>')
                    .build())
            .template(
                """
                                            Based on the following analysis data for <ticker>, produce your Peter Lynch–style investment signal.

                                            Analysis Data for <ticker>:
                                            <analysis_data>

                                            Return the trading signal in the following JSON format exactly:
                                            {{
                                              "ticker": the company ticker,
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

  public static boolean stringContainsItemFromList(String inputStr, List<String> items) {
    return items.stream().anyMatch(inputStr::contains);
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
