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
import java.util.stream.IntStream;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.garethjevans.ai.fd.LineItem;
import org.garethjevans.ai.fd.Metrics;
import org.garethjevans.ai.fd.Period;
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
  public Map<String, PeterLynchSignal> performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Peter Lynch's principles and LLM reasoning.");

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusMonths(3);

    List<String> tickers = List.of(ticker);

    Map<String, PeterLynchSignal> peterLynchAnalysis = new HashMap<>();

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
      var insiderTrades = financialDatasets.getInsiderTrades(ticker, null, endDate, 50);
      LOGGER.info("Got insider trades: {}", insiderTrades);

      updateProgress(t, "Fetching company news");
      var companyNews = financialDatasets.getCompanyNews(ticker, null, endDate, 50);
      LOGGER.info("Got company news: {}", companyNews);

      updateProgress(t, "Fetching recent price data for reference");
      var prices = financialDatasets.getPrices(ticker, startDate, endDate);
      LOGGER.info("Got prices: {}", prices);

      //        # Perform sub-analyses:
      //        progress.update_status("peter_lynch_agent", ticker, "Analyzing growth")
      //        growth_analysis = analyze_lynch_growth(financial_line_items)
      //
      //        progress.update_status("peter_lynch_agent", ticker, "Analyzing fundamentals")
      //        fundamentals_analysis = analyze_lynch_fundamentals(financial_line_items)
      //
      //        progress.update_status("peter_lynch_agent", ticker, "Analyzing valuation (focus on
      // PEG)")
      //        valuation_analysis = analyze_lynch_valuation(financial_line_items, market_cap)
      //
      //        progress.update_status("peter_lynch_agent", ticker, "Analyzing sentiment")
      //        sentiment_analysis = analyze_sentiment(company_news)
      //
      //        progress.update_status("peter_lynch_agent", ticker, "Analyzing insider activity")
      //        insider_activity = analyze_insider_activity(insider_trades)
      //
      //        # Combine partial scores with weights typical for Peter Lynch:
      //        #   30% Growth, 25% Valuation, 20% Fundamentals,
      //        #   15% Sentiment, 10% Insider Activity = 100%
      //        total_score = (
      //            growth_analysis["score"] * 0.30
      //            + valuation_analysis["score"] * 0.25
      //            + fundamentals_analysis["score"] * 0.20
      //            + sentiment_analysis["score"] * 0.15
      //            + insider_activity["score"] * 0.10
      //        )
      //
      //        max_possible_score = 10.0
      //
      //        # Map final score to signal
      //        if total_score >= 7.5:
      //            signal = "bullish"
      //        elif total_score <= 4.5:
      //            signal = "bearish"
      //        else:
      //            signal = "neutral"
      //
      //        analysis_data[ticker] = {
      //            "signal": signal,
      //            "score": total_score,
      //            "max_score": max_possible_score,
      //            "growth_analysis": growth_analysis,
      //            "valuation_analysis": valuation_analysis,
      //            "fundamentals_analysis": fundamentals_analysis,
      //            "sentiment_analysis": sentiment_analysis,
      //            "insider_activity": insider_activity,
      //        }
      //
      //        progress.update_status("peter_lynch_agent", ticker, "Generating Peter Lynch
      // analysis")
      //        lynch_output = generate_lynch_output(
      //            ticker=ticker,
      //            analysis_data=analysis_data[ticker],
      //            model_name=state["metadata"]["model_name"],
      //            model_provider=state["metadata"]["model_provider"],
      //        )
      //
      //        lynch_analysis[ticker] = {
      //            "signal": lynch_output.signal,
      //            "confidence": lynch_output.confidence,
      //            "reasoning": lynch_output.reasoning,
      //        }
      //
      //        progress.update_status("peter_lynch_agent", ticker, "Done")
      //
      //    # Wrap up results
      //    message = HumanMessage(content=json.dumps(lynch_analysis), name="peter_lynch_agent")
      //
      //    if state["metadata"].get("show_reasoning"):
      //        show_agent_reasoning(lynch_analysis, "Peter Lynch Agent")
      //
      //    # Save signals to state
      //    state["data"]["analyst_signals"]["peter_lynch_agent"] = lynch_analysis
      //
      //    progress.update_status("peter_lynch_agent", None, "Done")
      //
      //    return {"messages": [message], "data": state["data"]}
      //
      //
      // def analyze_lynch_growth(financial_line_items: list) -> dict:
      //    """
      //    Evaluate growth based on revenue and EPS trends:
      //      - Consistent revenue growth
      //      - Consistent EPS growth
      //    Peter Lynch liked companies with steady, understandable growth,
      //    often searching for potential 'ten-baggers' with a long runway.
      //    """
      //    if not financial_line_items or len(financial_line_items) < 2:
      //        return {"score": 0, "details": "Insufficient financial data for growth analysis"}
      //
      //    details = []
      //    raw_score = 0  # We'll sum up points, then scale to 0–10 eventually
      //
      //    # 1) Revenue Growth
      //    revenues = [fi.revenue for fi in financial_line_items if fi.revenue is not None]
      //    if len(revenues) >= 2:
      //        latest_rev = revenues[0]
      //        older_rev = revenues[-1]
      //        if older_rev > 0:
      //            rev_growth = (latest_rev - older_rev) / abs(older_rev)
      //            if rev_growth > 0.25:
      //                raw_score += 3
      //                details.append(f"Strong revenue growth: {rev_growth:.1%}")
      //            elif rev_growth > 0.10:
      //                raw_score += 2
      //                details.append(f"Moderate revenue growth: {rev_growth:.1%}")
      //            elif rev_growth > 0.02:
      //                raw_score += 1
      //                details.append(f"Slight revenue growth: {rev_growth:.1%}")
      //            else:
      //                details.append(f"Flat or negative revenue growth: {rev_growth:.1%}")
      //        else:
      //            details.append("Older revenue is zero/negative; can't compute revenue growth.")
      //    else:
      //        details.append("Not enough revenue data to assess growth.")
      //
      //    # 2) EPS Growth
      //    eps_values = [fi.earnings_per_share for fi in financial_line_items if
      // fi.earnings_per_share is not None]
      //    if len(eps_values) >= 2:
      //        latest_eps = eps_values[0]
      //        older_eps = eps_values[-1]
      //        if abs(older_eps) > 1e-9:
      //            eps_growth = (latest_eps - older_eps) / abs(older_eps)
      //            if eps_growth > 0.25:
      //                raw_score += 3
      //                details.append(f"Strong EPS growth: {eps_growth:.1%}")
      //            elif eps_growth > 0.10:
      //                raw_score += 2
      //                details.append(f"Moderate EPS growth: {eps_growth:.1%}")
      //            elif eps_growth > 0.02:
      //                raw_score += 1
      //                details.append(f"Slight EPS growth: {eps_growth:.1%}")
      //            else:
      //                details.append(f"Minimal or negative EPS growth: {eps_growth:.1%}")
      //        else:
      //            details.append("Older EPS is near zero; skipping EPS growth calculation.")
      //    else:
      //        details.append("Not enough EPS data for growth calculation.")
      //
      //    # raw_score can be up to 6 => scale to 0–10
      //    final_score = min(10, (raw_score / 6) * 10)
      //    return {"score": final_score, "details": "; ".join(details)}
      //
      //
      // def analyze_lynch_fundamentals(financial_line_items: list) -> dict:
      //    """
      //    Evaluate basic fundamentals:
      //      - Debt/Equity
      //      - Operating margin (or gross margin)
      //      - Positive Free Cash Flow
      //    Lynch avoided heavily indebted or complicated businesses.
      //    """
      //    if not financial_line_items:
      //        return {"score": 0, "details": "Insufficient fundamentals data"}
      //
      //    details = []
      //    raw_score = 0  # We'll accumulate up to 6 points, then scale to 0–10
      //
      //    # 1) Debt-to-Equity
      //    debt_values = [fi.total_debt for fi in financial_line_items if fi.total_debt is not
      // None]
      //    eq_values = [fi.shareholders_equity for fi in financial_line_items if
      // fi.shareholders_equity is not None]
      //    if debt_values and eq_values and len(debt_values) == len(eq_values) and len(debt_values)
      // >
      // 0:
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
      //    # 2) Operating Margin
      //    om_values = [fi.operating_margin for fi in financial_line_items if fi.operating_margin
      // is
      // not None]
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
      //
      //    # 3) Positive Free Cash Flow
      //    fcf_values = [fi.free_cash_flow for fi in financial_line_items if fi.free_cash_flow is
      // not
      // None]
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
      //
      //
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
      //    details = []
      //    raw_score = 0
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
      //
      //
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
      //    details = []
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
      //
      //
      // def analyze_insider_activity(insider_trades: list) -> dict:
      //    """
      //    Simple insider-trade analysis:
      //      - If there's heavy insider buying, it's a positive sign.
      //      - If there's mostly selling, it's a negative sign.
      //      - Otherwise, neutral.
      //    """
      //    # Default 5 (neutral)
      //    score = 5
      //    details = []
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
      //
      //
      // def generate_lynch_output(
      //    ticker: str,
      //    analysis_data: dict[str, any],
      //    model_name: str,
      //    model_provider: str,
      // ) -> PeterLynchSignal:
      //    """
      //    Generates a final JSON signal in Peter Lynch's voice & style.
      //    """
      //    template = ChatPromptTemplate.from_messages(
      //        [
      //            (
      //                "system",
      //                """You are a Peter Lynch AI agent. You make investment decisions based on
      // Peter Lynch's well-known principles:
      //
      //                1. Invest in What You Know: Emphasize understandable businesses, possibly
      // discovered in everyday life.
      //                2. Growth at a Reasonable Price (GARP): Rely on the PEG ratio as a prime
      // metric.
      //                3. Look for 'Ten-Baggers': Companies capable of growing earnings and share
      // price substantially.
      //                4. Steady Growth: Prefer consistent revenue/earnings expansion, less concern
      // about short-term noise.
      //                5. Avoid High Debt: Watch for dangerous leverage.
      //                6. Management & Story: A good 'story' behind the stock, but not overhyped or
      // too complex.
      //
      //                When you provide your reasoning, do it in Peter Lynch's voice:
      //                - Cite the PEG ratio
      //                - Mention 'ten-bagger' potential if applicable
      //                - Refer to personal or anecdotal observations (e.g., "If my kids love the
      // product...")
      //                - Use practical, folksy language
      //                - Provide key positives and negatives
      //                - Conclude with a clear stance (bullish, bearish, or neutral)
      //
      //                Return your final output strictly in JSON with the fields:
      //                {{
      //                  "signal": "bullish" | "bearish" | "neutral",
      //                  "confidence": 0 to 100,
      //                  "reasoning": "string"
      //                }}
      //                """,
      //            ),
      //            (
      //                "human",
      //                """Based on the following analysis data for {ticker}, produce your Peter
      // Lynch–style investment signal.
      //
      //                Analysis Data:
      //                {analysis_data}
      //
      //                Return only valid JSON with "signal", "confidence", and "reasoning".
      //                """,
      //            ),
      //        ]
      //    )
      //
      //    prompt = template.invoke({"analysis_data": json.dumps(analysis_data, indent=2),
      // "ticker":
      // ticker})
      //
      //    def create_default_signal():
      //        return PeterLynchSignal(
      //            signal="neutral",
      //            confidence=0.0,
      //            reasoning="Error in analysis; defaulting to neutral"
      //        )
      //
      //    return call_llm(
      //        prompt=prompt,
      //        model_name=model_name,
      //        model_provider=model_provider,
      //        pydantic_model=PeterLynchSignal,
      //        agent_name="peter_lynch_agent",
      //        default_factory=create_default_signal,
      //    )

      //    data = state["data"]

      updateProgress(t, "Analyzing fundamentals");
      var fundamentalAnalysis = analyzeFundamentals(metrics);
      LOGGER.info("Got fundamental analysis: {}", fundamentalAnalysis);

      updateProgress(t, "Analyzing consistency");
      var consistencyAnalysis = analyzeConsistency(financialLineItems);
      LOGGER.info("Got consistency analysis: {}", consistencyAnalysis);

      updateProgress(t, "Analyzing moat");
      var moatAnalysis = analyzeMoat(metrics);
      LOGGER.info("Got moat analysis: {}", moatAnalysis);

      updateProgress(t, "Analyzing management quality");
      var mgmtAnalysis = analyzeManagementQuality(financialLineItems);
      LOGGER.info("Got management quality analysis: {}", mgmtAnalysis);

      updateProgress(t, "Calculating intrinsic value");
      IntrinsicValueAnalysisResult intrinsicValueAnalysis =
          calculateIntrinsicValue(financialLineItems);
      LOGGER.info("Got intrinsic value analysis: {}", intrinsicValueAnalysis);

      // Calculate total score
      BigDecimal totalScore =
          fundamentalAnalysis
              .score()
              .add(consistencyAnalysis.score())
              .add(moatAnalysis.score())
              .add(mgmtAnalysis.score());

      BigDecimal maxPossibleScore =
          consistencyAnalysis.maxScore().add(moatAnalysis.maxScore()).add(mgmtAnalysis.maxScore());

      LOGGER.info("Got a score of {} out of a total {}", totalScore, maxPossibleScore);

      // Add margin of safety analysis if we have both intrinsic value and current price
      BigDecimal marginOfSafety = null;
      BigDecimal intrinsicValue = intrinsicValueAnalysis.intrinsicValue();
      LOGGER.info("Got intrinsic value: {}", intrinsicValue);

      if (intrinsicValue != null && marketCap != null) {
        marginOfSafety =
            intrinsicValue.add(marketCap.negate()).divide(marketCap, 2, RoundingMode.HALF_UP);
      }

      LOGGER.info("Got margin of safety: {}", marginOfSafety);

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

      LOGGER.info("Estimating signal as {}", signal);

      AnalysisResult analysisResult =
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
              marginOfSafety);

      updateProgress(t, "Generating Peter Lynch analysis");

      PeterLynchSignal output = generatePeterLynchOutput(t, analysisResult, toolContext);

      // Store analysis in consistent format with other agents
      peterLynchAnalysis.put(ticker, output);

      updateProgress(t, "Done");

      updateProgress(null, "Done");
    }

    return peterLynchAnalysis;
  }

  /**
   * Analyze company fundamentals based on Buffett's criteria.
   *
   * @param metrics
   * @return
   */
  public FundamentalsResult analyzeFundamentals(List<Metrics> metrics) {
    if (metrics == null || metrics.isEmpty()) {
      return new FundamentalsResult(
          BigDecimal.ZERO, new BigDecimal(7), "Insufficient fundamental data", null);
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
        new BigDecimal(score), new BigDecimal(7), String.join("; ", reasoning), latestMetrics);
  }

  public Result analyzeConsistency(List<LineItem> lineItems) {
    if (lineItems.size() < 4) {
      return new Result(new BigDecimal(0), new BigDecimal(3), "Insufficient historical data");
    }

    int score = 0;
    List<String> reasoning = new ArrayList<>();

    // Check earnings growth trend
    List<BigDecimal> earningsValues =
        lineItems.stream()
            .filter(l -> l.get("net_income") != null)
            .map(l -> l.get("net_income"))
            .toList();

    LOGGER.info("Earnings Values {}", earningsValues);

    if (earningsValues.size() >= 4) {
      // Simple check: is each period's earnings bigger than the next?
      boolean earningsGrowth =
          IntStream.range(1, earningsValues.size() - 1)
              .allMatch(p -> earningsValues.get(p - 1).compareTo(earningsValues.get(p)) > 0);

      if (earningsGrowth) {
        score += 3;
        reasoning.add("Consistent earnings growth over past periods");
      } else {
        reasoning.add("Inconsistent earnings growth pattern");
      }
    }

    // Calculate total growth rate from oldest to latest
    if (earningsValues.size() >= 2 && earningsValues.getLast().compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal growthRate =
          earningsValues
              .getFirst()
              .add(earningsValues.getLast().negate())
              .divide(earningsValues.getLast().abs(), 2, RoundingMode.HALF_UP);
      reasoning.add(
          "Total earnings growth of "
              + growthRate
              + " over past "
              + earningsValues.size()
              + " periods");
    } else {
      reasoning.add("Insufficient earnings data for trend analysis");
    }

    return new Result(new BigDecimal(score), new BigDecimal(3), String.join("; ", reasoning));
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

    LOGGER.info("LineItem> {}", latest);

    BigDecimal netIncome = latest.get("net_income");
    BigDecimal depreciation = latest.get("depreciation_and_amortization");
    BigDecimal capex = latest.get("capital_expenditure");

    if (netIncome == null || depreciation == null || capex == null) {
      LOGGER.info("netIncome: {}, depreciation: {}, capex: {}", netIncome, depreciation, capex);
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
      BigDecimal presentValue =
          futureEarnings.divide(
              BigDecimal.ONE.add(discountRate).pow(year), 2, RoundingMode.HALF_UP);
      futureValue = futureValue.add(presentValue);
    }

    BigDecimal terminalValue =
        ownerEarnings
            .multiply(BigDecimal.ONE.add(growthRate).pow(projectionYears))
            .multiply(terminalMultiple)
            .divide(BigDecimal.ONE.add(discountRate).pow(projectionYears), 2, RoundingMode.HALF_UP);

    BigDecimal intrinsicValue = futureValue.add(terminalValue);

    return new IntrinsicValueAnalysisResult(
        intrinsicValue,
        ownerEarnings,
        new Assumptions(growthRate, discountRate, terminalMultiple, projectionYears),
        "Intrinsic value calculated using DCF model with owner earnings");
  }

  private PeterLynchSignal generatePeterLynchOutput(
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
      return objectMapper.readValue(withoutMarkdown, PeterLynchSignal.class);
    } catch (JsonProcessingException e) {
      LOGGER.warn("Error in analysis, defaulting to neutral", e);
      return new PeterLynchSignal(Signal.neutral, 0f, "Error in analysis, defaulting to neutral");
    }
  }

  private String removeMarkdown(String in) {
    return in.replace("```json", "").replace("```", "").trim();
  }

  public String generateSystemMessage() {
    String body =
        """
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
      @JsonProperty("score") BigDecimal score,
      @JsonProperty("max_score") BigDecimal maxScore,
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

  public record PeterLynchSignal(
      @JsonProperty("signal") Signal signal,
      @JsonProperty("confidence") Float confidence,
      @JsonProperty("reasoning") String reasoning) {}

  public enum Signal {
    bullish,
    bearish,
    neutral
  }
}
