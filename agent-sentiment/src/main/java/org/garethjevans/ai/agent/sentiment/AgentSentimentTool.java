package org.garethjevans.ai.agent.sentiment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.garethjevans.ai.common.AgentSignal;
import org.garethjevans.ai.common.Signal;
import org.garethjevans.ai.fd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentSentimentTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentSentimentTool.class);

  private static final String AGENT_NAME = "Sentiment Agent";

  private final FinancialDatasetsService financialDatasets;

  public AgentSentimentTool(FinancialDatasetsService financialDatasets) {
    this.financialDatasets = financialDatasets;
  }

  @Tool(
      name = "sentiment_analysis",
      description = "Performs stock analysis using Sentiment methods by ticker")
  public AgentSignal performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Sentiment principles.");

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = LocalDate.now().minusYears(1);
    List<String> reasoning = new ArrayList<>();

    // Initialize sentiment analysis for each ticker
    //    sentiment_analysis = {}

    updateProgress(ticker, "Getting insider trades");
    var insiderTrades = financialDatasets.getInsiderTrades(ticker, startDate, endDate, 1000);
    LOGGER.info("Got insider trades: {}", insiderTrades);

    updateProgress(ticker, "Analyzing trading patterns");

    // Get the signals from the insider trades
    List<BigDecimal> transactionShares =
        insiderTrades.stream()
            .map(InsiderTrade::transactionShares)
            .filter(Objects::nonNull)
            .toList();

    List<Signal> insiderSignals =
        transactionShares.stream()
            .map(s -> s.compareTo(BigDecimal.ZERO) < 0 ? Signal.bearish : Signal.bullish)
            .toList();

    // Get the company news
    updateProgress(ticker, "Fetching company news");
    var companyNews = financialDatasets.getCompanyNews(ticker, startDate, endDate, 100);

    // Get the sentiment from the company news

    List<String> sentiment =
        companyNews.stream().map(CompanyNews::sentiment).filter(Objects::nonNull).toList();

    List<Signal> newsSignals =
        sentiment.stream()
            .map(
                s -> {
                  if (s.equalsIgnoreCase("negative")) {
                    return Signal.bearish;
                  } else if (s.equalsIgnoreCase("positive")) {
                    return Signal.bullish;
                  } else {
                    return Signal.neutral;
                  }
                })
            .toList();

    // Combine signals from both sources with weights
    updateProgress(ticker, "Combining signals");

    BigDecimal insiderWeight = new BigDecimal("0.3");
    BigDecimal newsWeight = new BigDecimal("0.7");

    // Calculate weighted signal counts
    BigDecimal bullishSignals =
        new BigDecimal(
                insiderSignals.stream().filter(signal -> signal.equals(Signal.bullish)).count())
            .multiply(insiderWeight)
            .add(
                new BigDecimal(
                        newsSignals.stream()
                            .filter(signal -> signal.equals(Signal.bullish))
                            .count())
                    .multiply(newsWeight));
    BigDecimal bearishSignals =
        new BigDecimal(
                insiderSignals.stream().filter(signal -> signal.equals(Signal.bearish)).count())
            .multiply(insiderWeight)
            .add(
                new BigDecimal(
                        newsSignals.stream()
                            .filter(signal -> signal.equals(Signal.bearish))
                            .count())
                    .multiply(newsWeight));

    Signal overall = null;

    if (bullishSignals.compareTo(bearishSignals) > 0) {
      overall = Signal.bullish;
    } else if (bearishSignals.compareTo(bullishSignals) > 0) {
      overall = Signal.bearish;
    } else {
      overall = Signal.neutral;
    }

    // Calculate confidence level based on the weighted proportion
    BigDecimal totalWeightedSignals =
        new BigDecimal(insiderSignals.size())
            .multiply(insiderWeight)
            .add(new BigDecimal(newsSignals.size()).multiply(newsWeight));

    // Default confidence when there are no signals
    float confidence = 0f;

    if (totalWeightedSignals.compareTo(BigDecimal.ZERO) > 0) {
      confidence =
          bullishSignals
              .max(bearishSignals)
              .divide(totalWeightedSignals, 2, RoundingMode.HALF_EVEN)
              .multiply(new BigDecimal(100))
              .floatValue();
    }

    reasoning.add("Weighted Bullish signals: " + bullishSignals);
    reasoning.add("Weighted Bearish signals: " + bearishSignals);

    return new AgentSignal(AGENT_NAME, ticker, overall, confidence, String.join("; ", reasoning));
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: {} - {}", AGENT_NAME, ticker, message);
  }
}
