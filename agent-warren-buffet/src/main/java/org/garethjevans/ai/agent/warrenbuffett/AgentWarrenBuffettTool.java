package org.garethjevans.ai.agent.warrenbuffett;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.garethjevans.ai.fd.LineItem;
import org.garethjevans.ai.fd.Metrics;
import org.garethjevans.ai.fd.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentWarrenBuffettTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentWarrenBuffettTool.class);

  private static final String AGENT_NAME = "Warren Buffet Agent";

  private final FinancialDatasetsService financialDatasets;

  public AgentWarrenBuffettTool(FinancialDatasetsService financialDatasets) {
    this.financialDatasets = financialDatasets;
  }

  @Tool(
      name = "warren_buffett_analysis",
      description = "Performs stock analysis using Warren Buffett's methods by ticker")
  public Map<String, AnalysisResult> performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker) {
    LOGGER.info("Analyzes stocks using Buffett's principles and LLM reasoning.");

    //    data = state["data"]
    LocalDate endDate = LocalDate.now();
    List<String> tickers = List.of(ticker);

    Map<String, AnalysisResult> analysisData = new HashMap<>();

    //    buffett_analysis = {}

    for (String t : tickers) {
      updateProgress(t, "Fetching financial metrics");
      List<Metrics> metrics = financialDatasets.getFinancialMetrics(ticker, endDate, Period.ttm, 5);

      updateProgress(t, "Gathering financial line items");
      List<LineItem> financialLineItems =
          financialDatasets.searchLineItems(
              ticker,
              endDate,
              List.of(
                  "capital_expenditure",
                  "depreciation_and_amortization",
                  "net_income",
                  "outstanding_shares",
                  "total_assets",
                  "total_liabilities",
                  "dividends_and_other_cash_distributions",
                  "issuance_or_purchase_of_equity_shares"),
              Period.ttm,
              10);

      updateProgress(t, "Getting market cap");
      var marketCap = financialDatasets.getMarketCap(ticker, endDate);

      updateProgress(t, "Analyzing fundamentals");
      var fundamentalAnalysis = analyzeFundamentals(metrics);

      updateProgress(t, "Analyzing consistency");
      var consistencyAnalysis = analyzeConsistency(financialLineItems);

      updateProgress(t, "Analyzing moat");
      var moatAnalysis = analyzeMoat(metrics);

      updateProgress(t, "Analyzing management quality");
      var mgmtAnalysis = analyzeManagementQuality(financialLineItems);

      updateProgress(t, "Calculating intrinsic value");
      IntrinsicValueAnalysisResult intrinsicValueAnalysis =
          calculateIntrinsicValue(financialLineItems);

      // Calculate total score
      BigDecimal totalScore =
          fundamentalAnalysis
              .score()
              .add(consistencyAnalysis.score())
              .add(moatAnalysis.score())
              .add(mgmtAnalysis.score());
      BigDecimal maxPossibleScore =
          new BigDecimal(10).add(moatAnalysis.maxScore()).add(mgmtAnalysis.maxScore());

      // Add margin of safety analysis if we have both intrinsic value and current price
      BigDecimal marginOfSafety = null;
      BigDecimal intrinsicValue = intrinsicValueAnalysis.intrinsicValue();

      if (intrinsicValue != null && marketCap != null) {
        marginOfSafety = intrinsicValue.add(marketCap.negate()).divide(marketCap);
      }

      Signal signal = null;
      if ((totalScore.compareTo(new BigDecimal("0.7").multiply(maxPossibleScore)) >= 0)
          && (marginOfSafety != null)
          && (marginOfSafety.compareTo(new BigDecimal("0.3")) >= 0)) {
        signal = Signal.bullish;
      } else if ((totalScore.compareTo(new BigDecimal("0.3").multiply(maxPossibleScore)) <= 0)
          || (marginOfSafety != null && marginOfSafety.compareTo(new BigDecimal("-0.3")) < 0)) {
        signal = Signal.bearish;
      } else {
        signal = Signal.neutral;
      }

      analysisData.put(
          t,
          new AnalysisResult(
              signal,
              totalScore,
              maxPossibleScore,
              fundamentalAnalysis,
              consistencyAnalysis,
              moatAnalysis,
              mgmtAnalysis,
              intrinsicValueAnalysis,
              marketCap,
              marginOfSafety));

      updateProgress(t, "Generating Warren Buffett analysis");

      return analysisData;
      // FIXME need to calculate the buffett output
      //    buffett_output = generate_buffett_output(
      //            ticker=ticker,
      //            analysis_data=analysis_data,
      //            model_name=state["metadata"]["model_name"],
      //            model_provider=state["metadata"]["model_provider"],
      //            )
      //
      //        # Store analysis in consistent format with other agents
      //    buffett_analysis[ticker] = {
      //        "signal": buffett_output.signal,
      //                "confidence": buffett_output.confidence,  # Normalize between 0 to 100
      //        "reasoning": buffett_output.reasoning,
      //    }
      //
      // updateProgress(t, "Done");
      //
      //            # Create the message
      //            message = HumanMessage(content=json.dumps(buffett_analysis),
      // name="warren_buffett_agent")
      //
      //    # Show reasoning if requested
      //    if state["metadata"]["show_reasoning"]:
      //    show_agent_reasoning(buffett_analysis, "Warren Buffett Agent")
      //
      //    # Add the signal to the analyst_signals list
      //    state["data"]["analyst_signals"]["warren_buffett_agent"] = buffett_analysis
      //
      // updateProgress(null, "Done");

      //            return {"messages": [message], "data": state["data"]}
    }
    return analysisData;
  }

  /**
   * Analyze company fundamentals based on Buffett's criteria.
   *
   * @param metrics
   * @return
   */
  public FundamentalsResult analyzeFundamentals(List<Metrics> metrics) {
    if (metrics == null || metrics.isEmpty()) {
      return new FundamentalsResult(BigDecimal.ZERO, "Insufficient fundamental data", null);
    }

    Metrics latestMetrics = metrics.get(0);

    int score = 0;
    List<String> reasoning = new ArrayList<>();

    if (latestMetrics.returnOnEquity() != null
        && latestMetrics.returnOnEquity().compareTo(new BigDecimal("0.15")) > 0) {
      score += 2;
      reasoning.add("Strong ROE of " + latestMetrics.returnOnEquity());
    } else if (latestMetrics.returnOnEquity() != null) {
      reasoning.add("Weak ROE of " + latestMetrics.returnOnEquity());
    } else {
      reasoning.add("ROE data not available");
    }

    // Check Debt to Equity
    if (latestMetrics.debtToEquity() != null
        && latestMetrics.debtToEquity().compareTo(new BigDecimal("0.5")) < 0) {
      score += 2;
      reasoning.add("Conservative debt levels");
    } else if (latestMetrics.debtToEquity() != null) {
      reasoning.add("High debt to equity ratio of " + latestMetrics.debtToEquity());
    } else {
      reasoning.add("Debt to equity data not available");
    }

    // Check Operating Margin
    if (latestMetrics.operatingMargin() != null
        && latestMetrics.operatingMargin().compareTo(new BigDecimal("0.15")) > 0) {
      score += 2;
      reasoning.add("Strong operating margins");
    } else if (latestMetrics.operatingMargin() != null) {
      reasoning.add("Weak operating margin of " + latestMetrics.operatingMargin());
    } else {
      reasoning.add("Operating margin data not available");
    }

    //  Check Current Ratio
    if (latestMetrics.currentRatio() != null
        && latestMetrics.currentRatio().compareTo(new BigDecimal("1.5")) > 0) {
      score += 1;
      reasoning.add("Good liquidity position");
    } else if (latestMetrics.currentRatio() != null) {
      reasoning.add("Weak liquidity with current ratio of " + latestMetrics.currentRatio());
    } else {
      reasoning.add("Current ratio data not available");
    }

    return new FundamentalsResult(
        new BigDecimal(score), String.join("; ", reasoning), latestMetrics);
  }

  public Result analyzeConsistency(List<LineItem> financialLineItems) {
    if (financialLineItems.size() < 4) {
      return new Result(new BigDecimal(0), null, "Insufficient historical data");
    }

    int score = 0;
    List<String> reasoning = new ArrayList<>();
    //
    //            # Check earnings growth trend
    //    earnings_values = [item.net_income for item in financial_line_items if item.net_income]
    //            if len(earnings_values) >= 4:
    //            # Simple check: is each period's earnings bigger than the next?
    //    earnings_growth = all(earnings_values[i] > earnings_values[i + 1] for i in
    // range(len(earnings_values) - 1))
    //
    //            if earnings_growth:
    //    score += 3
    //            reasoning.append("Consistent earnings growth over past periods")
    //            else:
    //            reasoning.append("Inconsistent earnings growth pattern")
    //
    //            # Calculate total growth rate from oldest to latest
    //        if len(earnings_values) >= 2 and earnings_values[-1] != 0:
    //    growth_rate = (earnings_values[0] - earnings_values[-1]) / abs(earnings_values[-1])
    //            reasoning.append(f"Total earnings growth of {growth_rate:.1%} over past
    // {len(earnings_values)} periods")
    //            else:
    //            reasoning.append("Insufficient earnings data for trend analysis")
    //
    return new Result(new BigDecimal(score), null, String.join("; ", reasoning));
  }

  /**
   * Evaluate whether the company likely has a durable competitive advantage (moat). For simplicity,
   * we look at stability of ROE/operating margins over multiple periods or high margin over the
   * last few years. Higher stability => higher moat score.
   *
   * @param metrics
   * @return
   */
  public Result analyzeMoat(List<Metrics> metrics) {
    if (metrics.size() < 3) {
      return new Result(
          new BigDecimal(0), new BigDecimal(3), "Insufficient data for moat analysis");
    }

    List<String> reasoning = new ArrayList<>();
    int moatScore = 0;

    List<BigDecimal> historicalRoes = new ArrayList<>();
    List<BigDecimal> historicalMargins = new ArrayList<>();

    metrics.stream()
        .filter(m -> m.returnOnEquity() != null)
        .forEach(m -> historicalRoes.add(m.returnOnEquity()));
    metrics.stream()
        .filter(m -> m.operatingMargin() != null)
        .forEach(m -> historicalMargins.add(m.operatingMargin()));

    if (historicalRoes.size() >= 3) {
      boolean stableRoe =
          historicalRoes.stream().allMatch(r -> r.compareTo(new BigDecimal("0.15")) > 0);
      if (stableRoe) {
        moatScore += 1;
        reasoning.add("Stable ROE above 15% across periods (suggests moat)");
      } else {
        reasoning.add("ROE not consistently above 15%");
      }
    }

    if (historicalMargins.size() >= 3) {
      boolean stableMargin =
          historicalMargins.stream().allMatch(r -> r.compareTo(new BigDecimal("0.15")) > 0);
      if (stableMargin) {
        moatScore += 1;
        reasoning.add("Stable operating margin above 15% across periods (suggests moat)");
      } else {
        reasoning.add("Operating not consistently above 15%");
      }
    }

    if (moatScore == 2) {
      moatScore += 1;
      reasoning.add("Both ROE and margin stability indicate a solid moat");
    }

    return new Result(new BigDecimal(moatScore), new BigDecimal(3), String.join(", ", reasoning));
  }

  /**
   * Checks for share dilution or consistent buybacks, and some dividend track record. A simplified
   * approach: - if there's net share repurchase or stable share count, it suggests management might
   * be shareholder-friendly. - if there's a big new issuance, it might be a negative sign
   * (dilution).
   *
   * @param lineItems
   * @return
   */
  public Result analyzeManagementQuality(List<LineItem> lineItems) {
    if (lineItems == null || lineItems.isEmpty()) {
      return new Result(
          new BigDecimal(0), new BigDecimal(2), "Insufficient data for management analysis");
    }

    int score = 0;
    List<String> reasoning = new ArrayList<>();

    LineItem latest = lineItems.get(0);
    if (latest.get("issuance_or_purchase_of_equity_shares") != null
        && latest.get("issuance_or_purchase_of_equity_shares").compareTo(BigDecimal.ZERO) < 0) {
      // Negative means the company spent money on buybacks
      score += 1;
      reasoning.add("Company has been repurchasing shares (shareholder-friendly)");
    }

    if (latest.get("issuance_or_purchase_of_equity_shares") != null
        && latest.get("issuance_or_purchase_of_equity_shares").compareTo(BigDecimal.ZERO) > 0) {
      reasoning.add("Recent common stock issuance (potential dilution)");
    } else {
      reasoning.add("No significant new stock issuance detected");
    }

    if (latest.get("dividends_and_other_cash_distributions") != null
        && latest.get("dividends_and_other_cash_distributions").compareTo(BigDecimal.ZERO) < 0) {
      score += 1;
      reasoning.add("Company has a track record of paying dividends");
    } else {
      reasoning.add("No or minimal dividends paid");
    }

    return new Result(new BigDecimal(score), new BigDecimal(2), String.join("; ", reasoning));
  }

  /**
   * Calculate owner earnings (Buffett's preferred measure of true earnings power). Owner Earnings =
   * Net Income + Depreciation - Maintenance CapEx
   *
   * @param lineItems
   * @return
   */
  public OwnerEarningsResult calculateOwnerEarnings(List<LineItem> lineItems) {
    if (lineItems == null || lineItems.isEmpty()) {
      return new OwnerEarningsResult(
          null, null, "Insufficient data for owner earnings calculation");
    }

    LineItem latest = lineItems.get(0);
    BigDecimal netIncome = latest.get("net_income");
    BigDecimal depreciation = latest.get("depreciation");
    BigDecimal capex = latest.get("capex");

    if (netIncome == null || depreciation == null || capex == null) {
      return new OwnerEarningsResult(
          null, null, "Missing components for owner earnings calculation");
    }

    // Estimate maintenance capex (typically 70-80% of total capex)
    BigDecimal maintenanceCapex = capex.multiply(new BigDecimal("0.75"));
    BigDecimal ownerEarnings = netIncome.add(depreciation).add(maintenanceCapex.negate());

    return new OwnerEarningsResult(
        ownerEarnings,
        new Components(netIncome, depreciation, maintenanceCapex),
        "Owner earnings calculated successfully");
  }

  /**
   * Calculate intrinsic value using DCF with owner earnings.
   *
   * @param lineItems
   * @return
   */
  public IntrinsicValueAnalysisResult calculateIntrinsicValue(List<LineItem> lineItems) {
    if (lineItems == null || lineItems.isEmpty()) {
      return new IntrinsicValueAnalysisResult(null, null, null, "Insufficient data for valuation");
    }

    // Calculate owner earnings
    OwnerEarningsResult earningsData = calculateOwnerEarnings(lineItems);
    if (earningsData.ownerEarnings() == null) {
      return new IntrinsicValueAnalysisResult(null, null, null, earningsData.details());
    }

    BigDecimal ownerEarnings = earningsData.ownerEarnings();

    // Get current market data
    LineItem latest = lineItems.get(0);
    BigDecimal sharesOutstanding = latest.get("outstanding_shares");

    if (sharesOutstanding == null) {
      return new IntrinsicValueAnalysisResult(null, null, null, "Missing shares outstanding data");
    }

    // Buffett's DCF assumptions (conservative approach)
    // Conservative 5% growth
    BigDecimal growthRate = new BigDecimal("0.05");
    // Typical ~9% discount rate
    BigDecimal discountRate = new BigDecimal("0.09");
    BigDecimal terminalMultiple = new BigDecimal(12);
    int projectionYears = 10;

    // Sum of discounted future owner earnings
    BigDecimal futureValue = BigDecimal.ZERO;

    for (int year = 1; year < projectionYears + 1; year++) {
      BigDecimal futureEarnings = ownerEarnings.multiply(BigDecimal.ONE.add(growthRate).pow(year));
      BigDecimal presentValue = futureEarnings.divide(BigDecimal.ONE.add(discountRate)).pow(year);
      futureValue = futureValue.add(presentValue);
    }

    BigDecimal terminalValue =
        ownerEarnings
            .multiply(BigDecimal.ONE.add(growthRate).pow(projectionYears))
            .multiply(terminalMultiple)
            .divide(BigDecimal.ONE.add(discountRate))
            .pow(projectionYears);

    BigDecimal intrinsicValue = futureValue.add(terminalValue);

    return new IntrinsicValueAnalysisResult(
        intrinsicValue,
        ownerEarnings,
        new Assumptions(growthRate, discountRate, terminalMultiple, projectionYears),
        "Intrinsic value calculated using DCF model with owner earnings");
  }

  //            # Default fallback signal in case parsing fails
  //    def create_default_warren_buffett_signal():
  //            return WarrenBuffettSignal(signal="neutral", confidence=0.0, reasoning="Error in
  // analysis, defaulting to neutral")
  //
  //    return call_llm(
  //            prompt=prompt,
  //            model_name=model_name,
  //            model_provider=model_provider,
  //            pydantic_model=WarrenBuffettSignal,
  //            agent_name="warren_buffett_agent",
  //            default_factory=create_default_warren_buffett_signal,
  //            )

  public String generateSystemMessage() {
    return """
                You are a Warren Buffett AI agent. Decide on investment signals based on Warren Buffett's principles:
                - Circle of Competence: Only invest in businesses you understand
                - Margin of Safety (> 30%): Buy at a significant discount to intrinsic value
                - Economic Moat: Look for durable competitive advantages
                - Quality Management: Seek conservative, shareholder-oriented teams
                - Financial Strength: Favor low debt, strong returns on equity
                - Long-term Horizon: Invest in businesses, not just stocks
                - Sell only if fundamentals deteriorate or valuation far exceeds intrinsic value

                When providing your reasoning, be thorough and specific by:
                1. Explaining the key factors that influenced your decision the most (both positive and negative)
                2. Highlighting how the company aligns with or violates specific Buffett principles
                3. Providing quantitative evidence where relevant (e.g., specific margins, ROE values, debt levels)
                4. Concluding with a Buffett-style assessment of the investment opportunity
                5. Using Warren Buffett's voice and conversational style in your explanation

                For example, if bullish: "I'm particularly impressed with [specific strength], reminiscent of our early investment in See's Candies where we saw [similar attribute]..."
                For example, if bearish: "The declining returns on capital remind me of the textile operations at Berkshire that we eventually exited because..."

                Follow these guidelines strictly.
                """;
  }

  public String generateUserMessage(String ticker, String analysisData) {
    PromptTemplate promptTemplate =
        PromptTemplate.builder()
            .renderer(
                StTemplateRenderer.builder()
                    .startDelimiterToken('{')
                    .endDelimiterToken('}')
                    .build())
            .template(
                """
                            Based on the following data, create the investment signal as Warren Buffett would:

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

    return promptTemplate.render(Map.of("ticker", ticker, "analysis_data", "{}"));
  }

  public String generateOutput(ChatClient client) {
    return null;
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: {} - {}", AGENT_NAME, ticker, message);
  }

  public record Result(BigDecimal score, BigDecimal maxScore, String details) {}

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
      @JsonProperty("details") String details,
      @JsonProperty("metrics") Metrics metrics) {}

  public record AnalysisResult(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("score") BigDecimal score,
      @JsonProperty("max_score") BigDecimal maxScore,
      @JsonProperty("fundamental_analysis") FundamentalsResult fundamentalAnalysis,
      @JsonProperty("consistency_analysis") Result consistencyAnalysis,
      @JsonProperty("moat_analysis") Result moatAnalysis,
      @JsonProperty("management_analysis") Result managementAnalysis,
      @JsonProperty("intrinsic_value_analysis") IntrinsicValueAnalysisResult intrinsicValueAnalysis,
      @JsonProperty("market_cap") BigDecimal marketCap,
      @JsonProperty("margin_of_safety") BigDecimal marginOfSafety) {}

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

  public record WarrenBuffetSignal(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("float") Float confidence,
      @JsonProperty("reasoning") String reasoning) {}

  public enum Signal {
    bullish,
    bearish,
    neutral
  }
}
