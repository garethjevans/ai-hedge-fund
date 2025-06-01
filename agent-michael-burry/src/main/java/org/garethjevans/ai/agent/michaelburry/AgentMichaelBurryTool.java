package org.garethjevans.ai.agent.michaelburry;

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

  /**
   * Analyse stocks using Michael Burry's deep‑value, contrarian framework.
   *
   * @param ticker
   * @param toolContext
   * @return
   */
  @Tool(
      name = "michael_burry_analysis",
      description = "Performs stock analysis using Michael Burry methods by ticker")
  public Map<String, MichaelBurrySignal> performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Michael Burry's principles and LLM reasoning.");

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusYears(1);

    List<String> tickers = List.of(ticker);

    Map<String, MichaelBurrySignal> burryAnalysis = new HashMap<>();

    for (String t : tickers) {
      updateProgress(t, "Fetching financial metrics");
      List<Metrics> metrics = financialDatasets.getFinancialMetrics(ticker, endDate, Period.ttm, 5);

      updateProgress(t, "Gathering financial line items");
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

      updateProgress(t, "Getting insider trades");
      var insiderTrades = financialDatasets.getInsiderTrades(ticker, startDate, endDate, 1000);
      LOGGER.info("Got insider trades: {}", insiderTrades);

      updateProgress(t, "Getting news");
      var news = financialDatasets.getCompanyNews(ticker, startDate, endDate, 1000);
      LOGGER.info("Got news: {}", news);

      updateProgress(t, "Getting market cap");
      var marketCap = financialDatasets.getMarketCap(ticker, endDate);
      LOGGER.info("Got market cap: {}", marketCap);

      //      updateProgress(t, "Analyzing fundamentals");
      //      var fundamentalAnalysis = analyzeFundamentals(metrics);
      //      LOGGER.info("Got fundamental analysis: {}", fundamentalAnalysis);

      //  ------------------------------------------------------------------
      //  Run sub‑analyses
      //  ------------------------------------------------------------------
      updateProgress(t, "Analyzing Value");
      var valueAnalysis = analyzeValue(metrics, lineItems, marketCap);
      LOGGER.info("Got Value Analysis {}", valueAnalysis);

      updateProgress(t, "Analyzing Balance Sheet");
      var balanceSheetAnalysis = analyzeBalanceSheet(metrics, lineItems);
      LOGGER.info("Got Balance Sheet Analysis {}", balanceSheetAnalysis);

      updateProgress(t, "Analyzing Insider activity");
      var insiderAnalysis = analyseInsiderActivity(insiderTrades);
      LOGGER.info("Got Insider Activity {}", insiderAnalysis);

      updateProgress(t, "Analyzing Contrarian Sentiment");
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

      // progress.update_status("michael_burry_agent", ticker, "Generating LLM output")
      //  burry_output = _generate_burry_output(
      //            ticker=ticker,
      //            analysis_data=analysis_data,
      //            model_name=state["metadata"]["model_name"],
      //            model_provider=state["metadata"]["model_provider"],
      //        )
      //
      //        burry_analysis[ticker] = {
      //            "signal": burry_output.signal,
      //            "confidence": burry_output.confidence,
      //            "reasoning": burry_output.reasoning,
      //        }
      //
      //        progress.update_status("michael_burry_agent", ticker, "Done")
      //
      //    # ----------------------------------------------------------------------
      //    # Return to the graph
      //    # ----------------------------------------------------------------------
      //    message = HumanMessage(content=json.dumps(burry_analysis), name="michael_burry_agent")
      //
      //    if state["metadata"].get("show_reasoning"):
      //        show_agent_reasoning(burry_analysis, "Michael Burry Agent")
      //
      //    state["data"]["analyst_signals"]["michael_burry_agent"] = burry_analysis
      //
      //    progress.update_status("michael_burry_agent", None, "Done")
      //
      //    return {"messages": [message], "data": state["data"]}
      //
      //
      // ###############################################################################
      // # Sub‑analysis helpers
      // ###############################################################################
      //
      LOGGER.info("Got a score of {} out of a total {}", totalScore, maxScore);

      updateProgress(t, "Generating Michael Burry analysis");

      MichaelBurrySignal output = generateOutput(t, analysisResult, toolContext);

      // Store analysis in consistent format with other agents
      burryAnalysis.put(ticker, output);

      updateProgress(t, "Done");

      updateProgress(null, "Done");
    }

    return burryAnalysis;
  }

  //    """Free cash‑flow yield, EV/EBIT, other classic deep‑value metrics."""
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
        details.add("Extraordinary FCF yield {fcf_yield:.1%}");
      } else if (fcfYield.compareTo(new BigDecimal("0.12")) >= 0) {
        score += 3;
        details.add("Very high FCF yield {fcf_yield:.1%}");
      } else if (fcfYield.compareTo(new BigDecimal("0.08")) >= 0) {
        score += 2;
        details.add("Respectable FCF yield {fcf_yield:.1%}");
      } else {
        details.add("Low FCF yield {fcf_yield:.1%}");
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
          details.add("EV/EBIT {ev_ebit:.1f} (<6)");
        } else if (evEbit.compareTo(new BigDecimal(10)) < 0) {
          score += 1;
          details.add("EV/EBIT {ev_ebit:.1f} (<10)");
        } else {
          details.add("High EV/EBIT {ev_ebit:.1f}");
        }
      } else {
        details.add("EV/EBIT data unavailable");
      }
    } else {
      details.add("Financial metrics unavailable");
    }

    return new Result(score, maxScore, String.join("; ", details));
  }

  //
  // ----- Balance sheet --------------------------------------------------------
  public Result analyzeBalanceSheet(List<Metrics> metric, List<LineItem> lineItem) {
    // def _analyze_balance_sheet(metrics, line_items):
    //    """Leverage and liquidity checks."""
    //
    int maxScore = 3;
    int score = 0;
    List<String> details = new ArrayList<>();
    //
    //    latest_metrics = metrics[0] if metrics else None
    //    latest_item = _latest_line_item(line_items)
    //
    //    debt_to_equity = getattr(latest_metrics, "debt_to_equity", None) if latest_metrics else
    // None
    //    if debt_to_equity is not None:
    //        if debt_to_equity < 0.5:
    //            score += 2
    //            details.add(f"Low D/E {debt_to_equity:.2f}")
    //        elif debt_to_equity < 1:
    //            score += 1
    //            details.add(f"Moderate D/E {debt_to_equity:.2f}")
    //        else:
    //            details.add(f"High leverage D/E {debt_to_equity:.2f}")
    //    else:
    //        details.add("Debt‑to‑equity data unavailable")
    //
    //    # Quick liquidity sanity check (cash vs total debt)
    //    if latest_item is not None:
    //        cash = getattr(latest_item, "cash_and_equivalents", None)
    //        total_debt = getattr(latest_item, "total_debt", None)
    //        if cash is not None and total_debt is not None:
    //            if cash > total_debt:
    //                score += 1
    //                details.add("Net cash position")
    //            else:
    //                details.add("Net debt position")
    //        else:
    //            details.add("Cash/debt data unavailable")
    //
    //    return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    return new Result(score, maxScore, String.join("; ", details));
  }

  //
  // # ----- Insider activity -----------------------------------------------------
  //
  /** Net insider buying over the last 12 months acts as a hard catalyst.""" */
  public Result analyseInsiderActivity(List<InsiderTrade> insiderTrades) {
    // def _analyze_insider_activity(insider_trades):
    //
    int maxScore = 2;
    int score = 0;
    List<String> details = new ArrayList<>();

    //    max_score = 2
    //    score = 0
    //    details: list[str] = []
    //
    //    if not insider_trades:
    //        details.add("No insider trade data")
    //        return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //    shares_bought = sum(t.transaction_shares or 0 for t in insider_trades if
    // (t.transaction_shares or 0) > 0)
    //    shares_sold = abs(sum(t.transaction_shares or 0 for t in insider_trades if
    // (t.transaction_shares or 0) < 0))
    //    net = shares_bought - shares_sold
    //    if net > 0:
    //        score += 2 if net / max(shares_sold, 1) > 1 else 1
    //        details.add(f"Net insider buying of {net:,} shares")
    //    else:
    //        details.add("Net insider selling")
    //
    //    return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    return new Result(score, maxScore, String.join("; ", details));
  }

  public Result analyzeContrarianSentiment(List<CompanyNews> news) {
    // def _analyze_contrarian_sentiment(news):
    //    """Very rough gauge: a wall of recent negative headlines can be a *positive* for a
    // contrarian."""
    //
    int maxScore = 1;
    int score = 0;
    List<String> details = new ArrayList<>();
    //
    //    if not news:
    //        details.add("No recent news")
    //        return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //    # Count negative sentiment articles
    //    sentiment_negative_count = sum(
    //        1 for n in news if n.sentiment and n.sentiment.lower() in ["negative", "bearish"]
    //    )
    //
    //    if sentiment_negative_count >= 5:
    //        score += 1  # The more hated, the better (assuming fundamentals hold up)
    //        details.add(f"{sentiment_negative_count} negative headlines (contrarian
    // opportunity)")
    //    else:
    //        details.add("Limited negative press")
    //
    //    return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    return new Result(score, maxScore, String.join("; ", details));
  }

  //
  //
  // ###############################################################################
  // # LLM generation
  // ###############################################################################
  //
  // def _generate_burry_output(
  //    ticker: str,
  //    analysis_data: dict,
  //    *,
  //    model_name: str,
  //    model_provider: str,
  // ) -> MichaelBurrySignal:
  //    """Call the LLM to craft the final trading signal in Burry's voice."""
  //
  //    template = ChatPromptTemplate.from_messages(
  //        [
  //            (
  //                "system",

  //                """,
  //            ),
  //            (
  //                "human",
  //                """Based on the following data, create the investment signal as Michael
  // Burry
  // would:
  //
  //                Analysis Data for {ticker}:
  //                {analysis_data}
  //
  //                Return the trading signal in the following JSON format exactly:
  //                {{
  //                  "signal": "bullish" | "bearish" | "neutral",
  //                  "confidence": float between 0 and 100,
  //                  "reasoning": "string"
  //                }}
  //                """,
  //            ),
  //        ]
  //    )
  //
  //    prompt = template.invoke({"analysis_data": json.dumps(analysis_data, indent=2),
  // "ticker":
  // ticker})
  //
  //    # Default fallback signal in case parsing fails
  //    def create_default_michael_burry_signal():
  //        return MichaelBurrySignal(signal="neutral", confidence=0.0, reasoning="Parsing error
  // –
  // defaulting to neutral")
  //
  //    return call_llm(
  //        prompt=prompt,
  //        model_name=model_name,
  //        model_provider=model_provider,
  //        pydantic_model=MichaelBurrySignal,
  //        agent_name="michael_burry_agent",
  //        default_factory=create_default_michael_burry_signal,
  //    )

  //    data = state["data"]

  private MichaelBurrySignal generateOutput(
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
      return objectMapper.readValue(withoutMarkdown, MichaelBurrySignal.class);
    } catch (JsonProcessingException e) {
      LOGGER.warn("Error in analysis, defaulting to neutral", e);
      return new MichaelBurrySignal(Signal.neutral, 0f, "Error in analysis, defaulting to neutral");
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
                    .startDelimiterToken('{')
                    .endDelimiterToken('}')
                    .build())
            .template(
                """
                                            Based on the following data, create the investment signal as Michael Burry would:

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

  public record Result(
      @JsonProperty("score") int score,
      @JsonProperty("max_score") int maxScore,
      @JsonProperty("details") String details) {}

  public record OwnerEarningsResult(
      @JsonProperty("owner_earnings") BigDecimal ownerEarnings,
      @JsonProperty("components") Components components,
      @JsonProperty("details") String details) {}

  public record Components(
      @JsonProperty("net_income") BigDecimal netIncome,
      @JsonProperty("depreciation") BigDecimal depreciation,
      @JsonProperty("maintenance_capex") BigDecimal maintenanceCapex) {}

  public record FundamentalsResult(
      @JsonProperty("score") BigDecimal score,
      @JsonProperty("max_score") BigDecimal maxScore,
      @JsonProperty("details") String details,
      @JsonProperty("metrics") Metrics metrics) {}

  public record AnalysisResult(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("score") int score,
      @JsonProperty("max_score") int maxScore,
      @JsonProperty("value_analysis") Result valueAnalysis,
      @JsonProperty("balance_sheet_analysis") Result balanceSheetAnalysis,
      @JsonProperty("insider_analysis") Result insiderAnalysis,
      @JsonProperty("contrarian_analysis") Result contrarianAnalysis,
      @JsonProperty("market_cap") BigDecimal marketCap) {}

  public record IntrinsicValueAnalysisResult(
      @JsonProperty("intrinsic_value") BigDecimal intrinsicValue,
      @JsonProperty("owner_earnings") BigDecimal ownerEarnings,
      @JsonProperty("assumptions") Assumptions assumptions,
      @JsonProperty("details") String details) {}

  public record Assumptions(
      @JsonProperty("growth_rate") BigDecimal growthRate,
      @JsonProperty("discount_rate") BigDecimal discountRate,
      @JsonProperty("terminal_multiple") BigDecimal terminalMultiple,
      @JsonProperty("projection_years") int projectionYears) {}

  public record MichaelBurrySignal(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("confidence") Float confidence,
      @JsonProperty("reasoning") String reasoning) {}

  public enum Signal {
    bullish,
    bearish,
    neutral
  }
}
