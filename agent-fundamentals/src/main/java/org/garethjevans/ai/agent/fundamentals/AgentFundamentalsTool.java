package org.garethjevans.ai.agent.fundamentals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.garethjevans.ai.common.AgentSignal;
import org.garethjevans.ai.common.Signal;
import org.garethjevans.ai.fd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentFundamentalsTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentFundamentalsTool.class);

  private static final String AGENT_NAME = "Fundamentals Agent";

  private final FinancialDatasetsService financialDatasets;

  public AgentFundamentalsTool(FinancialDatasetsService financialDatasets) {
    this.financialDatasets = financialDatasets;
  }

  @Tool(
      name = "fundamentals_analysis",
      description = "Performs stock analysis using Fundamentals methods by ticker")
  public AgentSignal performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Fundamentals principles.");

    LocalDate endDate = LocalDate.now();

    updateProgress(ticker, "Fetching financial metrics");
    List<Metrics> metrics = financialDatasets.getFinancialMetrics(ticker, endDate, Period.ttm, 10);

    // Get the financial metrics
    if (metrics.isEmpty()) {
      updateProgress(ticker, "Failed: No financial metrics found");
    }

    // Pull the most recent financial metrics
    Metrics latest = metrics.get(0);

    // Initialize signals list for different fundamental aspects
    List<Signal> signals = new ArrayList<>();
    List<String> reasoning = new ArrayList<>();

    // 1. Profitability Analysis
    updateProgress(ticker, "Analyzing profitability");
    BigDecimal returnOnEquity = latest.returnOnEquity();
    BigDecimal netMargin = latest.netMargin();
    BigDecimal operatingMargin = latest.operatingMargin();

    Map<BigDecimal, BigDecimal> thresholds =
        Map.of(
            returnOnEquity, new BigDecimal("0.15"), // Strong ROE above 15%
            netMargin, new BigDecimal("0.20"), // Healthy profit margins
            operatingMargin, new BigDecimal("0.15") // Strong operating efficiency
            );

    long profitabilityScore =
        thresholds.entrySet().stream()
            .filter(e -> e.getKey() != null && e.getKey().compareTo(e.getValue()) > 0)
            .count();

    if (profitabilityScore >= 2) {
      signals.add(Signal.bullish);
    } else if (profitabilityScore > 0) {
      signals.add(Signal.neutral);
    } else {
      signals.add(Signal.bearish);
    }

    reasoning.add("ROE: " + returnOnEquity);
    reasoning.add("Net Margin: " + netMargin);
    reasoning.add("Op Margin: " + operatingMargin);

    // 2. Growth Analysis
    updateProgress(ticker, "Analyzing growth");
    BigDecimal revenueGrowth = latest.revenueGrowth();
    BigDecimal earningsGrowth = latest.earningsGrowth();
    BigDecimal bookValueGrowth = latest.bookValueGrowth();

    thresholds =
        Map.of(
            revenueGrowth, new BigDecimal("0.10"), // 10% revenue growth
            earningsGrowth, new BigDecimal("0.10"), // 10% earnings growth
            bookValueGrowth, new BigDecimal("0.10") // 10% book value growth
            );

    long growthScore =
        thresholds.entrySet().stream()
            .filter(e -> e.getKey() != null && e.getKey().compareTo(e.getValue()) > 0)
            .count();

    if (growthScore >= 2) {
      signals.add(Signal.bullish);
    } else if (growthScore > 0) {
      signals.add(Signal.neutral);
    } else {
      signals.add(Signal.bearish);
    }

    reasoning.add("Revenue Growth: " + revenueGrowth);
    reasoning.add("Earnings Growth: " + earningsGrowth);

    // 3. Financial Health
    updateProgress(ticker, "Analyzing financial health");
    BigDecimal currentRatio = latest.currentRatio();
    BigDecimal debtToEquity = latest.debtToEquity();
    BigDecimal freeCashFlowPerShare = latest.freeCashFlowPerShare();
    BigDecimal earningsPerShare = latest.earningsPerShare();

    int healthScore = 0;
    if (currentRatio != null && currentRatio.compareTo(new BigDecimal("1.5")) > 0) {
      // Strong liquidity
      healthScore += 1;
    }

    if (debtToEquity != null && debtToEquity.compareTo(new BigDecimal("0.5")) < 0) {
      // Conservative debt levels
      healthScore += 1;
    }

    if (freeCashFlowPerShare != null
        && earningsPerShare != null
        && freeCashFlowPerShare.compareTo(earningsPerShare.multiply(new BigDecimal("0.8"))) > 0) {
      // Strong FCF conversion
      healthScore += 1;
    }

    if (healthScore >= 2) {
      signals.add(Signal.bullish);
    } else if (healthScore > 0) {
      signals.add(Signal.neutral);
    } else {
      signals.add(Signal.bearish);
    }

    reasoning.add("Current Ratio: " + currentRatio);
    reasoning.add("Debt to Equity: " + debtToEquity);

    // 4. Price to X ratios
    updateProgress(ticker, "Analyzing valuation ratios");
    BigDecimal peRatio = latest.priceToEarningsRatio();
    BigDecimal pbRatio = latest.priceToBookRatio();
    BigDecimal psRatio = latest.priceToSalesRatio();

    thresholds =
        Map.of(
            peRatio, new BigDecimal("25"), // Reasonable P/E ratio
            pbRatio, new BigDecimal("3"), // Reasonable P/B ratio
            psRatio, new BigDecimal("5") // Reasonable P/S ratio
            );

    long priceRatioScore =
        thresholds.entrySet().stream()
            .filter(e -> e.getKey() != null && e.getKey().compareTo(e.getValue()) > 0)
            .count();

    if (priceRatioScore >= 2) {
      signals.add(Signal.bullish);
    } else if (priceRatioScore > 0) {
      signals.add(Signal.neutral);
    } else {
      signals.add(Signal.bearish);
    }

    reasoning.add("P/E: " + peRatio);
    reasoning.add("P/B: " + pbRatio);
    reasoning.add("P/S: " + psRatio);

    updateProgress(ticker, "Calculating final signal");

    // Determine overall signal
    long bullishSignals = signals.stream().filter(s -> s.equals(Signal.bullish)).count();
    long bearishSignals = signals.stream().filter(s -> s.equals(Signal.bearish)).count();

    Signal overall = null;
    if (bullishSignals > bearishSignals) {
      overall = Signal.bullish;
    } else if (bearishSignals > bullishSignals) {
      overall = Signal.bearish;
    } else {
      overall = Signal.neutral;
    }

    // Calculate confidence level
    int totalSignals = signals.size();
    float confidence = ((float) Math.max(bullishSignals, bearishSignals) / totalSignals) * 100;

    return new AgentSignal(ticker, overall, confidence, String.join("; ", reasoning));
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: {} - {}", AGENT_NAME, ticker, message);
  }
}
