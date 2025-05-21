package org.garethjevans.ai.agent.warrenbuffet;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;

public class AgentWarrenBuffetTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentWarrenBuffetTool.class);

  private static final String AGENT_NAME = "Warren Buffet Agent";

  private final FinancialDatasetsService financialDatasets;

  public AgentWarrenBuffetTool(FinancialDatasetsService financialDatasets) {
    this.financialDatasets = financialDatasets;
  }

  public void run() {
    LOGGER.info("Analyzes stocks using Buffett's principles and LLM reasoning.");

    //    data = state["data"]
    LocalDate endDate = LocalDate.now();
    List<String> tickers = List.of("AAPL");

    Map<String, AnalysisResult> analysisData = new HashMap<>();

    //    buffett_analysis = {}

    for (String ticker : tickers) {
      updateProgress(ticker, "Fetching financial metrics");
      List<FinancialDatasetsService.Metric> metrics =
          financialDatasets.getFinancialMetrics(ticker, endDate, "ttm", 5);

      updateProgress(ticker, "Gathering financial line items");
      List<FinancialDatasetsService.LineItem> financialLineItems =
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
              "ttm",
              10);

      updateProgress(ticker, "Getting market cap");
      var marketCap = financialDatasets.getMarketCap(ticker, endDate);

      updateProgress(ticker, "Analyzing fundamentals");
      var fundamentalAnalysis = analyzeFundamentals(metrics);

      updateProgress(ticker, "Analyzing consistency");
      var consistencyAnalysis = analyzeConsistency(financialLineItems);

      updateProgress(ticker, "Analyzing moat");
      var moatAnalysis = analyzeMoat(metrics);

      updateProgress(ticker, "Analyzing management quality");
      var mgmtAnalysis = analyzeManagementQuality(financialLineItems);

      updateProgress(ticker, "Calculating intrinsic value");
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

      analysisData.put(ticker, new AnalysisResult(signal, totalScore, maxPossibleScore));

      //            # Combine all analysis results
      //    analysis_data[ticker] = {
      //        "signal": signal,
      //                "score": total_score,
      //                "max_score": max_possible_score,
      //                "fundamental_analysis": fundamental_analysis,
      //                "consistency_analysis": consistency_analysis,
      //                "moat_analysis": moat_analysis,
      //                "management_analysis": mgmt_analysis,
      //                "intrinsic_value_analysis": intrinsic_value_analysis,
      //                "market_cap": market_cap,
      //                "margin_of_safety": margin_of_safety,
      //    }
      //
      updateProgress(ticker, "Generating Warren Buffett analysis");

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
      updateProgress(ticker, "Done");
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
      updateProgress(null, "Done");

      //            return {"messages": [message], "data": state["data"]}
    }
  }

  public Result analyzeFundamentals(List<FinancialDatasetsService.Metric> metrics) {
    //    def analyze_fundamentals(metrics: list) -> dict[str, any]:
    //            """Analyze company fundamentals based on Buffett's criteria."""
    //            if not metrics:
    //            return {"score": 0, "details": "Insufficient fundamental data"}
    //
    //    latest_metrics = metrics[0]
    //
    //    score = 0
    //    reasoning = []
    //
    //            # Check ROE (Return on Equity)
    //    if latest_metrics.return_on_equity and latest_metrics.return_on_equity > 0.15:  # 15% ROE
    // threshold
    //    score += 2
    //            reasoning.append(f"Strong ROE of {latest_metrics.return_on_equity:.1%}")
    //    elif latest_metrics.return_on_equity:
    //            reasoning.append(f"Weak ROE of {latest_metrics.return_on_equity:.1%}")
    //            else:
    //            reasoning.append("ROE data not available")
    //
    //            # Check Debt to Equity
    //    if latest_metrics.debt_to_equity and latest_metrics.debt_to_equity < 0.5:
    //    score += 2
    //            reasoning.append("Conservative debt levels")
    //    elif latest_metrics.debt_to_equity:
    //            reasoning.append(f"High debt to equity ratio of
    // {latest_metrics.debt_to_equity:.1f}")
    //            else:
    //            reasoning.append("Debt to equity data not available")
    //
    //            # Check Operating Margin
    //    if latest_metrics.operating_margin and latest_metrics.operating_margin > 0.15:
    //    score += 2
    //            reasoning.append("Strong operating margins")
    //    elif latest_metrics.operating_margin:
    //            reasoning.append(f"Weak operating margin of
    // {latest_metrics.operating_margin:.1%}")
    //            else:
    //            reasoning.append("Operating margin data not available")
    //
    //            # Check Current Ratio
    //    if latest_metrics.current_ratio and latest_metrics.current_ratio > 1.5:
    //    score += 1
    //            reasoning.append("Good liquidity position")
    //    elif latest_metrics.current_ratio:
    //            reasoning.append(f"Weak liquidity with current ratio of
    // {latest_metrics.current_ratio:.1f}")
    //            else:
    //            reasoning.append("Current ratio data not available")
    //
    //            return {"score": score, "details": "; ".join(reasoning), "metrics":
    // latest_metrics.model_dump()}
    //
    return null;
  }

  public Result analyzeConsistency(List<FinancialDatasetsService.LineItem> financialLineItems) {
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
    return new Result(new BigDecimal(score), null, String.join(", ", reasoning));
  }

  /**
   * Evaluate whether the company likely has a durable competitive advantage (moat). For simplicity,
   * we look at stability of ROE/operating margins over multiple periods or high margin over the
   * last few years. Higher stability => higher moat score.
   *
   * @param metrics
   * @return
   */
  public Result analyzeMoat(List<FinancialDatasetsService.Metric> metrics) {
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

  //
  //
  public Result analyzeManagementQuality(List<FinancialDatasetsService.LineItem> lineItems) {
    //            """
    //    Checks for share dilution or consistent buybacks, and some dividend track record.
    //    A simplified approach:
    //      - if there's net share repurchase or stable share count, it suggests management
    //        might be shareholder-friendly.
    //      - if there's a big new issuance, it might be a negative sign (dilution).
    //    """
    //            if not financial_line_items:
    //            return {"score": 0, "max_score": 2, "details": "Insufficient data for management
    // analysis"}
    //
    //    reasoning = []
    //    mgmt_score = 0
    //
    //    latest = financial_line_items[0]
    //            if hasattr(latest, "issuance_or_purchase_of_equity_shares") and
    // latest.issuance_or_purchase_of_equity_shares and latest.issuance_or_purchase_of_equity_shares
    // < 0:
    //            # Negative means the company spent money on buybacks
    //    mgmt_score += 1
    //            reasoning.append("Company has been repurchasing shares (shareholder-friendly)")
    //
    //            if hasattr(latest, "issuance_or_purchase_of_equity_shares") and
    // latest.issuance_or_purchase_of_equity_shares and latest.issuance_or_purchase_of_equity_shares
    // > 0:
    //            # Positive issuance means new shares => possible dilution
    //        reasoning.append("Recent common stock issuance (potential dilution)")
    //                else:
    //                reasoning.append("No significant new stock issuance detected")
    //
    //                # Check for any dividends
    //    if hasattr(latest, "dividends_and_other_cash_distributions") and
    // latest.dividends_and_other_cash_distributions and
    // latest.dividends_and_other_cash_distributions < 0:
    //    mgmt_score += 1
    //            reasoning.append("Company has a track record of paying dividends")
    //            else:
    //            reasoning.append("No or minimal dividends paid")
    //
    //            return {
    //        "score": mgmt_score,
    //                "max_score": 2,
    //                "details": "; ".join(reasoning),
    return null;
  }

  public Result calculateOwnerEarnings(List<FinancialDatasetsService.LineItem> lineItems) {
    //            """Calculate owner earnings (Buffett's preferred measure of true earnings power).
    //    Owner Earnings = Net Income + Depreciation - Maintenance CapEx"""
    //            if not financial_line_items or len(financial_line_items) < 1:
    //            return {"owner_earnings": None, "details": ["Insufficient data for owner earnings
    // calculation"]}
    //
    //    latest = financial_line_items[0]
    //
    //    net_income = latest.net_income
    //            depreciation = latest.depreciation_and_amortization
    //    capex = latest.capital_expenditure
    //
    //    if not all([net_income, depreciation, capex]):
    //            return {"owner_earnings": None, "details": ["Missing components for owner earnings
    // calculation"]}
    //
    //    # Estimate maintenance capex (typically 70-80% of total capex)
    //    maintenance_capex = capex * 0.75
    //    owner_earnings = net_income + depreciation - maintenance_capex
    //
    //    return {
    //        "owner_earnings": owner_earnings,
    //                "components": {"net_income": net_income, "depreciation": depreciation,
    // "maintenance_capex": maintenance_capex},
    //        "details": ["Owner earnings calculated successfully"],
    return null;
  }

  //
  //
  public IntrinsicValueAnalysisResult calculateIntrinsicValue(
      List<FinancialDatasetsService.LineItem> lineItems) {
    //            """Calculate intrinsic value using DCF with owner earnings."""
    //            if not financial_line_items:
    //            return {"intrinsic_value": None, "details": ["Insufficient data for valuation"]}
    //
    //    # Calculate owner earnings
    //            earnings_data = calculate_owner_earnings(financial_line_items)
    //    if not earnings_data["owner_earnings"]:
    //            return {"intrinsic_value": None, "details": earnings_data["details"]}
    //
    //    owner_earnings = earnings_data["owner_earnings"]
    //
    //            # Get current market data
    //    latest_financial_line_items = financial_line_items[0]
    //    shares_outstanding = latest_financial_line_items.outstanding_shares
    //
    //    if not shares_outstanding:
    //            return {"intrinsic_value": None, "details": ["Missing shares outstanding data"]}
    //
    //    # Buffett's DCF assumptions (conservative approach)
    //    growth_rate = 0.05  # Conservative 5% growth
    //            discount_rate = 0.09  # Typical ~9% discount rate
    //    terminal_multiple = 12
    //    projection_years = 10
    //
    //            # Sum of discounted future owner earnings
    //    future_value = 0
    //            for year in range(1, projection_years + 1):
    //    future_earnings = owner_earnings * (1 + growth_rate) ** year
    //            present_value = future_earnings / (1 + discount_rate) ** year
    //    future_value += present_value
    //
    //    # Terminal value
    //    terminal_value = (owner_earnings * (1 + growth_rate) ** projection_years *
    // terminal_multiple) / ((1 + discount_rate) ** projection_years)
    //
    //    intrinsic_value = future_value + terminal_value
    //
    //    return {
    //        "intrinsic_value": intrinsic_value,
    //                "owner_earnings": owner_earnings,
    //                "assumptions": {
    //            "growth_rate": growth_rate,
    //                    "discount_rate": discount_rate,
    //                    "terminal_multiple": terminal_multiple,
    //                    "projection_years": projection_years,
    //        },
    //        "details": ["Intrinsic value calculated using DCF model with owner earnings"],
    return null;
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

  public record AnalysisResult(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("score") BigDecimal score,
      @JsonProperty("max_score") BigDecimal maxScore) {}

  public record IntrinsicValueAnalysisResult(
      @JsonProperty("intrinsic_value") BigDecimal intrinsicValue,
      @JsonProperty("owner_earnings") BigDecimal ownerEarnings,
      @JsonProperty("assumptions") Assumptions assumptions,
      @JsonProperty("details") List<String> details) {}

  public record Assumptions(
      @JsonProperty("growth_rate") BigDecimal growthRate,
      @JsonProperty("discount_rate") BigDecimal discountRate,
      @JsonProperty("terminal_multiple") BigDecimal terminalMultiple,
      @JsonProperty("projection_years") BigDecimal projectionYears) {}

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
