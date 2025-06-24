package org.garethjevans.ai.agent.michaelburry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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

public class AgentMichaelBurryTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentMichaelBurryTool.class);

  private static final String AGENT_NAME = "Michael Burry Agent";

  private final FinancialDatasetsService financialDatasets;
  private final ObjectMapper objectMapper;

  public AgentMichaelBurryTool(
      FinancialDatasetsService financialDatasets, ObjectMapper objectMapper) {
    this.financialDatasets = financialDatasets;
    this.objectMapper = objectMapper;
  }

  /** Analyse stocks using Michael Burry's deep‑value, contrarian framework. */
  @Tool(
      name = "michael_burry_analysis",
      description = "Performs stock analysis using Michael Burry methods by ticker")
  public AgentSignal performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Michael Burry's principles and LLM reasoning.");

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusYears(1);

    updateProgress(ticker, "Fetching financial metrics");
    List<Metrics> metrics = financialDatasets.getFinancialMetrics(ticker, endDate, Period.ttm, 5);

    updateProgress(ticker, "Gathering financial line items");
    List<LineItem> lineItems =
        financialDatasets.searchLineItems(
            ticker,
            endDate,
            List.of(
                "free_cash_flow",
                "net_income",
                "total_debt",
                "cash_and_equivalents",
                "total_assets",
                "total_liabilities",
                "outstanding_shares",
                "issuance_or_purchase_of_equity_shares"),
            Period.ttm,
            10);

    updateProgress(ticker, "Getting insider trades");
    var insiderTrades = financialDatasets.getInsiderTrades(ticker, startDate, endDate, 1000);
    LOGGER.info("Got insider trades: {}", insiderTrades);

    updateProgress(ticker, "Getting news");
    var news = financialDatasets.getCompanyNews(ticker, startDate, endDate, 1000);
    LOGGER.info("Got news: {}", news);

    updateProgress(ticker, "Getting market cap");
    var marketCap = financialDatasets.getMarketCap(ticker, endDate);
    LOGGER.info("Got market cap: {}", marketCap);

    //  ------------------------------------------------------------------
    //  Run sub‑analyses
    //  ------------------------------------------------------------------
    updateProgress(ticker, "Analyzing Value");
    var valueAnalysis = analyzeValue(metrics, lineItems, marketCap);
    LOGGER.info("Got Value Analysis {}", valueAnalysis);

    updateProgress(ticker, "Analyzing Balance Sheet");
    var balanceSheetAnalysis = analyzeBalanceSheet(metrics, lineItems);
    LOGGER.info("Got Balance Sheet Analysis {}", balanceSheetAnalysis);

    updateProgress(ticker, "Analyzing Insider activity");
    var insiderAnalysis = analyseInsiderActivity(insiderTrades);
    LOGGER.info("Got Insider Activity {}", insiderAnalysis);

    updateProgress(ticker, "Analyzing Contrarian Sentiment");
    var contrarianAnalysis = analyzeContrarianSentiment(news);
    LOGGER.info("Got Contrarian Sentiment {}", contrarianAnalysis);

    // ------------------------------------------------------------------
    // Aggregate score & derive preliminary signal
    // ------------------------------------------------------------------
    int totalScore =
        valueAnalysis.score()
            + balanceSheetAnalysis.score()
            + insiderAnalysis.score()
            + contrarianAnalysis.score();

    int maxScore =
        valueAnalysis.maxScore()
            + balanceSheetAnalysis.maxScore()
            + insiderAnalysis.maxScore()
            + contrarianAnalysis.maxScore();

    Signal signal = null;

    if (new BigDecimal(totalScore)
            .compareTo(new BigDecimal(maxScore).multiply(new BigDecimal("0.7")))
        >= 0) {
      signal = Signal.bullish;
    } else if (new BigDecimal(totalScore)
            .compareTo(new BigDecimal(maxScore).multiply(new BigDecimal("0.3")))
        <= 0) {
      signal = Signal.bearish;
    } else {
      signal = Signal.neutral;
    }

    // ------------------------------------------------------------------
    // Collect data for LLM reasoning & output
    // ------------------------------------------------------------------
    AnalysisResult analysisResult =
        new AnalysisResult(
            signal,
            totalScore,
            maxScore,
            valueAnalysis,
            balanceSheetAnalysis,
            insiderAnalysis,
            contrarianAnalysis,
            marketCap);

    LOGGER.info("Got a score of {} out of a total {}", totalScore, maxScore);

    updateProgress(ticker, "Generating Michael Burry analysis");

    AgentSignal output = generateOutput(ticker, analysisResult, toolContext);

    updateProgress(ticker, "Done");

    return output;
  }

  /** Free cash‑flow yield, EV/EBIT, other classic deep‑value metrics. */
  public Result analyzeValue(
      List<Metrics> metrics, List<LineItem> lineItems, BigDecimal marketCap) {

    // 4 pts for FCF‑yield, 2 pts for EV/EBIT
    int maxScore = 6;
    int score = 0;
    List<String> details = new ArrayList<>();

    // Free‑cash‑flow yield
    LineItem latest = lineItems.get(0);
    BigDecimal fcf = latest.get("free_cash_flow");
    if (fcf != null && marketCap != null) {
      BigDecimal fcfYield = fcf.divide(marketCap, 2, RoundingMode.HALF_UP);
      if (fcfYield.compareTo(new BigDecimal("0.15")) >= 0) {
        score += 4;
        details.add("Extraordinary FCF yield " + fcfYield);
      } else if (fcfYield.compareTo(new BigDecimal("0.12")) >= 0) {
        score += 3;
        details.add("Very high FCF yield " + fcfYield);
      } else if (fcfYield.compareTo(new BigDecimal("0.08")) >= 0) {
        score += 2;
        details.add("Respectable FCF yield " + fcfYield);
      } else {
        details.add("Low FCF yield " + fcfYield);
      }
    } else {
      details.add("FCF data unavailable");
    }

    // EV/EBIT (from financial metrics)
    if (!metrics.isEmpty()) {
      BigDecimal evEbit = metrics.get(0).enterpriseValueToEbitdaRatio();
      if (evEbit != null) {
        if (evEbit.compareTo(new BigDecimal(6)) < 0) {
          score += 2;
          details.add("EV/EBIT " + evEbit + "(<6)");
        } else if (evEbit.compareTo(new BigDecimal(10)) < 0) {
          score += 1;
          details.add("EV/EBIT " + evEbit + " (<10)");
        } else {
          details.add("High EV/EBIT " + evEbit);
        }
      } else {
        details.add("EV/EBIT data unavailable");
      }
    } else {
      details.add("Financial metrics unavailable");
    }

    return new Result(score, maxScore, String.join("; ", details));
  }

  /** Leverage and liquidity checks. */
  public Result analyzeBalanceSheet(List<Metrics> metrics, List<LineItem> lineItem) {

    int maxScore = 3;
    int score = 0;
    List<String> details = new ArrayList<>();

    Metrics latestMetrics = metrics.get(0);
    LineItem latestLineItem = lineItem.get(0);

    BigDecimal debtToEquity = latestMetrics.debtToEquity();
    if (debtToEquity != null) {
      if (debtToEquity.compareTo(new BigDecimal("0.5")) < 0) {
        score += 2;
        details.add("Low D/E " + debtToEquity);
      } else if (debtToEquity.compareTo(new BigDecimal("1.0")) < 0) {
        score += 1;
        details.add("Moderate D/E " + debtToEquity);
      } else {
        details.add("High leverage D/E " + debtToEquity);
      }
    } else {
      details.add("Debt‑to‑equity data unavailable");
    }

    if (latestLineItem != null) {
      BigDecimal cash = latestLineItem.get("cash_and_equivalents");
      BigDecimal totalDebt = latestLineItem.get("total_debt");
      if (cash != null && totalDebt != null) {
        if (cash.compareTo(totalDebt) > 0) {
          score += 1;
          details.add("Net cash position");
        } else {
          details.add("Net debt position");
        }
      } else {
        details.add("Cash/debt data unavailable");
      }
    }

    return new Result(score, maxScore, String.join("; ", details));
  }

  /** Net insider buying over the last 12 months acts as a hard catalyst. */
  public Result analyseInsiderActivity(List<InsiderTrade> insiderTrades) {
    int maxScore = 2;
    int score = 0;
    List<String> details = new ArrayList<>();

    if (insiderTrades.isEmpty()) {
      details.add("No insider trade data");
      return new Result(score, maxScore, String.join("; ", details));
    }

    BigDecimal sharesBought =
        insiderTrades.stream()
            .map(t -> t.transactionShares() != null ? t.transactionShares() : BigDecimal.ZERO)
            .filter(shares -> shares.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal sharesSold =
        insiderTrades.stream()
            .map(t -> t.transactionShares() != null ? t.transactionShares() : BigDecimal.ZERO)
            .filter(shares -> shares.compareTo(BigDecimal.ZERO) < 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .abs();

    // net = shares_bought - shares_sold
    BigDecimal net = sharesBought.subtract(sharesSold);

    if (net.compareTo(BigDecimal.ZERO) > 0) {
      score +=
          (net.divide(sharesSold.max(BigDecimal.ONE), 2, RoundingMode.HALF_UP)
                      .compareTo(BigDecimal.ONE)
                  > 0
              ? 2
              : 1);
      details.add("Net insider buying of " + net + " shares");
    } else {
      details.add("Net insider selling");
    }

    return new Result(score, maxScore, String.join("; ", details));
  }

  /** Very rough gauge: a wall of recent negative headlines can be a *positive* for a contrarian. */
  public Result analyzeContrarianSentiment(List<CompanyNews> news) {

    int maxScore = 1;
    int score = 0;
    List<String> details = new ArrayList<>();

    if (news.isEmpty()) {
      details.add("No recent news");
      return new Result(score, maxScore, String.join("; ", details));
    }

    // Count negative sentiment articles
    long negativeCount =
        news.stream()
            .filter(
                n ->
                    "negative".equalsIgnoreCase(n.sentiment())
                        || "bearish".equalsIgnoreCase(n.sentiment()))
            .count();

    if (negativeCount >= 5) {
      score += 1;
      details.add(negativeCount + " negative headline(s) (contrarian opportunity)");
    } else {
      details.add("Limited negative press");
    }

    return new Result(score, maxScore, String.join("; ", details));
  }

  private AgentSignal generateOutput(
      String ticker, AnalysisResult analysisResult, ToolContext toolContext) {
    StringBuilder buffettOutput = new StringBuilder();

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

                buffettOutput.append(((McpSchema.TextContent) llmResponse.content()).text());
              }

              exchange.loggingNotification(
                  McpSchema.LoggingMessageNotification.builder()
                      .level(McpSchema.LoggingLevel.INFO)
                      .data("Finish Sampling")
                      .build());
            });

    String withoutMarkdown = removeMarkdown(buffettOutput.toString());
    LOGGER.info("Got sampling response '{}'", withoutMarkdown);

    try {
      return objectMapper.readValue(withoutMarkdown, AgentSignal.class).withAgent(AGENT_NAME);
    } catch (JsonProcessingException e) {
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
                    You are an AI agent emulating Dr. Michael J. Burry. Your mandate:
                    - Hunt for deep value in US equities using hard numbers (free cash flow, EV/EBIT, balance sheet)
                    - Be contrarian: hatred in the press can be your friend if fundamentals are solid
                    - Focus on downside first – avoid leveraged balance sheets
                    - Look for hard catalysts such as insider buying, buybacks, or asset sales
                    - Communicate in Burry's terse, data‑driven style

                    When providing your reasoning, be thorough and specific by:
                    1. Start with the key metric(s) that drove your decision
                    2. Cite concrete numbers (e.g. "FCF yield 14.7%", "EV/EBIT 5.3")
                    3. Highlight risk factors and why they are acceptable (or not)
                    4. Mention relevant insider activity or contrarian opportunities
                    5. Use Burry's direct, number-focused communication style with minimal words

                    For example, if bullish: "FCF yield 12.8%. EV/EBIT 6.2. Debt-to-equity 0.4. Net insider buying 25k shares. Market missing value due to overreaction to recent litigation. Strong buy."
                    For example, if bearish: "FCF yield only 2.1%. Debt-to-equity concerning at 2.3. Management diluting shareholders. Pass."
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
                                            Based on the following data, create the investment signal as Michael Burry would:

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

  public record AnalysisResult(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("score") int score,
      @JsonProperty("max_score") int maxScore,
      @JsonProperty("value_analysis") Result valueAnalysis,
      @JsonProperty("balance_sheet_analysis") Result balanceSheetAnalysis,
      @JsonProperty("insider_analysis") Result insiderAnalysis,
      @JsonProperty("contrarian_analysis") Result contrarianAnalysis,
      @JsonProperty("market_cap") BigDecimal marketCap) {}
}
