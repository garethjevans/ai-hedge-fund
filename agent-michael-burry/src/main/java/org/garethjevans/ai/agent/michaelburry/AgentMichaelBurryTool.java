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

  @Tool(
      name = "michael_burry_analysis",
      description = "Performs stock analysis using Michael Burry methods by ticker")
  public Map<String, MichaelBurrySignal> performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Michael Burry principles and LLM reasoning.");

    // from __future__ import annotations
    //
    //from datetime import datetime, timedelta
    //import json
    //from typing_extensions import Literal
    //
    //from src.graph.state import AgentState, show_agent_reasoning
    //from langchain_core.messages import HumanMessage
    //from langchain_core.prompts import ChatPromptTemplate
    //from pydantic import BaseModel
    //
    //from src.tools.api import (
    //    get_company_news,
    //    get_financial_metrics,
    //    get_insider_trades,
    //    get_market_cap,
    //    search_line_items,
    //)
    //from src.utils.llm import call_llm
    //from src.utils.progress import progress
    //
    //__all__ = [
    //    "MichaelBurrySignal",
    //    "michael_burry_agent",
    //]
    //
    //###############################################################################
    //# Pydantic output model
    //###############################################################################
    //
    //
    //class MichaelBurrySignal(BaseModel):
    //    """Schema returned by the LLM."""
    //
    //    signal: Literal["bullish", "bearish", "neutral"]
    //    confidence: float  # 0–100
    //    reasoning: str
    //
    //
    //###############################################################################
    //# Core agent
    //###############################################################################
    //
    //
    //def michael_burry_agent(state: AgentState):  # noqa: C901  (complexity is fine here)
    //    """Analyse stocks using Michael Burry's deep‑value, contrarian framework."""
    //
    //    data = state["data"]
    //    end_date: str = data["end_date"]  # YYYY‑MM‑DD
    //    tickers: list[str] = data["tickers"]
    //
    //    # We look one year back for insider trades / news flow
    //    start_date = (datetime.fromisoformat(end_date) - timedelta(days=365)).date().isoformat()
    //
    //    analysis_data: dict[str, dict] = {}
    //    burry_analysis: dict[str, dict] = {}
    //
    //    for ticker in tickers:
    //        # ------------------------------------------------------------------
    //        # Fetch raw data
    //        # ------------------------------------------------------------------
    //        progress.update_status("michael_burry_agent", ticker, "Fetching financial metrics")
    //        metrics = get_financial_metrics(ticker, end_date, period="ttm", limit=5)
    //
    //        progress.update_status("michael_burry_agent", ticker, "Fetching line items")
    //        line_items = search_line_items(
    //            ticker,
    //            [
    //                "free_cash_flow",
    //                "net_income",
    //                "total_debt",
    //                "cash_and_equivalents",
    //                "total_assets",
    //                "total_liabilities",
    //                "outstanding_shares",
    //                "issuance_or_purchase_of_equity_shares",
    //            ],
    //            end_date,
    //        )
    //
    //        progress.update_status("michael_burry_agent", ticker, "Fetching insider trades")
    //        insider_trades = get_insider_trades(ticker, end_date=end_date, start_date=start_date)
    //
    //        progress.update_status("michael_burry_agent", ticker, "Fetching company news")
    //        news = get_company_news(ticker, end_date=end_date, start_date=start_date, limit=250)
    //
    //        progress.update_status("michael_burry_agent", ticker, "Fetching market cap")
    //        market_cap = get_market_cap(ticker, end_date)
    //
    //        # ------------------------------------------------------------------
    //        # Run sub‑analyses
    //        # ------------------------------------------------------------------
    //        progress.update_status("michael_burry_agent", ticker, "Analyzing value")
    //        value_analysis = _analyze_value(metrics, line_items, market_cap)
    //
    //        progress.update_status("michael_burry_agent", ticker, "Analyzing balance sheet")
    //        balance_sheet_analysis = _analyze_balance_sheet(metrics, line_items)
    //
    //        progress.update_status("michael_burry_agent", ticker, "Analyzing insider activity")
    //        insider_analysis = _analyze_insider_activity(insider_trades)
    //
    //        progress.update_status("michael_burry_agent", ticker, "Analyzing contrarian sentiment")
    //        contrarian_analysis = _analyze_contrarian_sentiment(news)
    //
    //        # ------------------------------------------------------------------
    //        # Aggregate score & derive preliminary signal
    //        # ------------------------------------------------------------------
    //        total_score = (
    //            value_analysis["score"]
    //            + balance_sheet_analysis["score"]
    //            + insider_analysis["score"]
    //            + contrarian_analysis["score"]
    //        )
    //        max_score = (
    //            value_analysis["max_score"]
    //            + balance_sheet_analysis["max_score"]
    //            + insider_analysis["max_score"]
    //            + contrarian_analysis["max_score"]
    //        )
    //
    //        if total_score >= 0.7 * max_score:
    //            signal = "bullish"
    //        elif total_score <= 0.3 * max_score:
    //            signal = "bearish"
    //        else:
    //            signal = "neutral"
    //
    //        # ------------------------------------------------------------------
    //        # Collect data for LLM reasoning & output
    //        # ------------------------------------------------------------------
    //        analysis_data[ticker] = {
    //            "signal": signal,
    //            "score": total_score,
    //            "max_score": max_score,
    //            "value_analysis": value_analysis,
    //            "balance_sheet_analysis": balance_sheet_analysis,
    //            "insider_analysis": insider_analysis,
    //            "contrarian_analysis": contrarian_analysis,
    //            "market_cap": market_cap,
    //        }
    //
    //        progress.update_status("michael_burry_agent", ticker, "Generating LLM output")
    //        burry_output = _generate_burry_output(
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
    //###############################################################################
    //# Sub‑analysis helpers
    //###############################################################################
    //
    //
    //def _latest_line_item(line_items: list):
    //    """Return the most recent line‑item object or *None*."""
    //    return line_items[0] if line_items else None
    //
    //
    //# ----- Value ----------------------------------------------------------------
    //
    //def _analyze_value(metrics, line_items, market_cap):
    //    """Free cash‑flow yield, EV/EBIT, other classic deep‑value metrics."""
    //
    //    max_score = 6  # 4 pts for FCF‑yield, 2 pts for EV/EBIT
    //    score = 0
    //    details: list[str] = []
    //
    //    # Free‑cash‑flow yield
    //    latest_item = _latest_line_item(line_items)
    //    fcf = getattr(latest_item, "free_cash_flow", None) if latest_item else None
    //    if fcf is not None and market_cap:
    //        fcf_yield = fcf / market_cap
    //        if fcf_yield >= 0.15:
    //            score += 4
    //            details.append(f"Extraordinary FCF yield {fcf_yield:.1%}")
    //        elif fcf_yield >= 0.12:
    //            score += 3
    //            details.append(f"Very high FCF yield {fcf_yield:.1%}")
    //        elif fcf_yield >= 0.08:
    //            score += 2
    //            details.append(f"Respectable FCF yield {fcf_yield:.1%}")
    //        else:
    //            details.append(f"Low FCF yield {fcf_yield:.1%}")
    //    else:
    //        details.append("FCF data unavailable")
    //
    //    # EV/EBIT (from financial metrics)
    //    if metrics:
    //        ev_ebit = getattr(metrics[0], "ev_to_ebit", None)
    //        if ev_ebit is not None:
    //            if ev_ebit < 6:
    //                score += 2
    //                details.append(f"EV/EBIT {ev_ebit:.1f} (<6)")
    //            elif ev_ebit < 10:
    //                score += 1
    //                details.append(f"EV/EBIT {ev_ebit:.1f} (<10)")
    //            else:
    //                details.append(f"High EV/EBIT {ev_ebit:.1f}")
    //        else:
    //            details.append("EV/EBIT data unavailable")
    //    else:
    //        details.append("Financial metrics unavailable")
    //
    //    return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //
    //# ----- Balance sheet --------------------------------------------------------
    //
    //def _analyze_balance_sheet(metrics, line_items):
    //    """Leverage and liquidity checks."""
    //
    //    max_score = 3
    //    score = 0
    //    details: list[str] = []
    //
    //    latest_metrics = metrics[0] if metrics else None
    //    latest_item = _latest_line_item(line_items)
    //
    //    debt_to_equity = getattr(latest_metrics, "debt_to_equity", None) if latest_metrics else None
    //    if debt_to_equity is not None:
    //        if debt_to_equity < 0.5:
    //            score += 2
    //            details.append(f"Low D/E {debt_to_equity:.2f}")
    //        elif debt_to_equity < 1:
    //            score += 1
    //            details.append(f"Moderate D/E {debt_to_equity:.2f}")
    //        else:
    //            details.append(f"High leverage D/E {debt_to_equity:.2f}")
    //    else:
    //        details.append("Debt‑to‑equity data unavailable")
    //
    //    # Quick liquidity sanity check (cash vs total debt)
    //    if latest_item is not None:
    //        cash = getattr(latest_item, "cash_and_equivalents", None)
    //        total_debt = getattr(latest_item, "total_debt", None)
    //        if cash is not None and total_debt is not None:
    //            if cash > total_debt:
    //                score += 1
    //                details.append("Net cash position")
    //            else:
    //                details.append("Net debt position")
    //        else:
    //            details.append("Cash/debt data unavailable")
    //
    //    return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //
    //# ----- Insider activity -----------------------------------------------------
    //
    //def _analyze_insider_activity(insider_trades):
    //    """Net insider buying over the last 12 months acts as a hard catalyst."""
    //
    //    max_score = 2
    //    score = 0
    //    details: list[str] = []
    //
    //    if not insider_trades:
    //        details.append("No insider trade data")
    //        return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //    shares_bought = sum(t.transaction_shares or 0 for t in insider_trades if (t.transaction_shares or 0) > 0)
    //    shares_sold = abs(sum(t.transaction_shares or 0 for t in insider_trades if (t.transaction_shares or 0) < 0))
    //    net = shares_bought - shares_sold
    //    if net > 0:
    //        score += 2 if net / max(shares_sold, 1) > 1 else 1
    //        details.append(f"Net insider buying of {net:,} shares")
    //    else:
    //        details.append("Net insider selling")
    //
    //    return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //
    //# ----- Contrarian sentiment -------------------------------------------------
    //
    //def _analyze_contrarian_sentiment(news):
    //    """Very rough gauge: a wall of recent negative headlines can be a *positive* for a contrarian."""
    //
    //    max_score = 1
    //    score = 0
    //    details: list[str] = []
    //
    //    if not news:
    //        details.append("No recent news")
    //        return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //    # Count negative sentiment articles
    //    sentiment_negative_count = sum(
    //        1 for n in news if n.sentiment and n.sentiment.lower() in ["negative", "bearish"]
    //    )
    //
    //    if sentiment_negative_count >= 5:
    //        score += 1  # The more hated, the better (assuming fundamentals hold up)
    //        details.append(f"{sentiment_negative_count} negative headlines (contrarian opportunity)")
    //    else:
    //        details.append("Limited negative press")
    //
    //    return {"score": score, "max_score": max_score, "details": "; ".join(details)}
    //
    //
    //###############################################################################
    //# LLM generation
    //###############################################################################
    //
    //def _generate_burry_output(
    //    ticker: str,
    //    analysis_data: dict,
    //    *,
    //    model_name: str,
    //    model_provider: str,
    //) -> MichaelBurrySignal:
    //    """Call the LLM to craft the final trading signal in Burry's voice."""
    //
    //    template = ChatPromptTemplate.from_messages(
    //        [
    //            (
    //                "system",
    //                """You are an AI agent emulating Dr. Michael J. Burry. Your mandate:
    //                - Hunt for deep value in US equities using hard numbers (free cash flow, EV/EBIT, balance sheet)
    //                - Be contrarian: hatred in the press can be your friend if fundamentals are solid
    //                - Focus on downside first – avoid leveraged balance sheets
    //                - Look for hard catalysts such as insider buying, buybacks, or asset sales
    //                - Communicate in Burry's terse, data‑driven style
    //
    //                When providing your reasoning, be thorough and specific by:
    //                1. Start with the key metric(s) that drove your decision
    //                2. Cite concrete numbers (e.g. "FCF yield 14.7%", "EV/EBIT 5.3")
    //                3. Highlight risk factors and why they are acceptable (or not)
    //                4. Mention relevant insider activity or contrarian opportunities
    //                5. Use Burry's direct, number-focused communication style with minimal words
    //
    //                For example, if bullish: "FCF yield 12.8%. EV/EBIT 6.2. Debt-to-equity 0.4. Net insider buying 25k shares. Market missing value due to overreaction to recent litigation. Strong buy."
    //                For example, if bearish: "FCF yield only 2.1%. Debt-to-equity concerning at 2.3. Management diluting shareholders. Pass."
    //                """,
    //            ),
    //            (
    //                "human",
    //                """Based on the following data, create the investment signal as Michael Burry would:
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
    //    prompt = template.invoke({"analysis_data": json.dumps(analysis_data, indent=2), "ticker": ticker})
    //
    //    # Default fallback signal in case parsing fails
    //    def create_default_michael_burry_signal():
    //        return MichaelBurrySignal(signal="neutral", confidence=0.0, reasoning="Parsing error – defaulting to neutral")
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
    LocalDate endDate = LocalDate.now();
    List<String> tickers = List.of(ticker);

    Map<String, MichaelBurrySignal> buffettAnalysis = new HashMap<>();

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
      LOGGER.info("Got market cap: {}", marketCap);

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

      updateProgress(t, "Generating Warren Buffett analysis");

      MichaelBurrySignal buffettOutput = generateBuffettOutput(t, analysisResult, toolContext);

      // Store analysis in consistent format with other agents
      buffettAnalysis.put(ticker, buffettOutput);

      updateProgress(t, "Done");

      updateProgress(null, "Done");
    }

    return buffettAnalysis;
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

  private MichaelBurrySignal generateBuffettOutput(
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
