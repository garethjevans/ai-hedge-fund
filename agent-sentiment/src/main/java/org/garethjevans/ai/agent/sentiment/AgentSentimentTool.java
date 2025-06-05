package org.garethjevans.ai.agent.sentiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.garethjevans.ai.common.AgentSignal;
import org.garethjevans.ai.common.Signal;
import org.garethjevans.ai.fd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;

public class AgentSentimentTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentSentimentTool.class);

  private static final String AGENT_NAME = "Sentiment Agent";

  private final FinancialDatasetsService financialDatasets;
  private final ObjectMapper objectMapper;

  public AgentSentimentTool(FinancialDatasetsService financialDatasets, ObjectMapper objectMapper) {
    this.financialDatasets = financialDatasets;
    this.objectMapper = objectMapper;
  }

  @Tool(
      name = "sentiment_analysis",
      description = "Performs stock analysis using Sentiment methods by ticker")
  public AgentSignal performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Sentiment principles.");

    // ##### Sentiment Agent #####
    // def sentiment_analyst_agent(state: AgentState):
    //    """Analyzes market sentiment and generates trading signals for multiple tickers."""
    //    data = state.get("data", {})
    //    end_date = data.get("end_date")
    //    tickers = data.get("tickers")
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusYears(1);

    //
    //    # Initialize sentiment analysis for each ticker
    //    sentiment_analysis = {}
    //
    //    for ticker in tickers:
    //        progress.update_status("sentiment_analyst_agent", ticker, "Fetching insider trades")
    //
    //        # Get the insider trades
    //        insider_trades = get_insider_trades(
    //            ticker=ticker,
    //            end_date=end_date,
    //            limit=1000,
    //        )

    updateProgress(ticker, "Getting insider trades");
    var insiderTrades = financialDatasets.getInsiderTrades(ticker, startDate, endDate, 1000);
    LOGGER.info("Got insider trades: {}", insiderTrades);

    //
    //        progress.update_status("sentiment_analyst_agent", ticker, "Analyzing trading
    // patterns")
    //
    //        # Get the signals from the insider trades
    //        transaction_shares = pd.Series([t.transaction_shares for t in
    // insider_trades]).dropna()
    //        insider_signals = np.where(transaction_shares < 0, "bearish", "bullish").tolist()
    //
    //        progress.update_status("sentiment_analyst_agent", ticker, "Fetching company news")
    //
    //        # Get the company news
    //        company_news = get_company_news(ticker, end_date, limit=100)
    //
    //        # Get the sentiment from the company news
    //        sentiment = pd.Series([n.sentiment for n in company_news]).dropna()
    //        news_signals = np.where(sentiment == "negative", "bearish",
    //                              np.where(sentiment == "positive", "bullish",
    // "neutral")).tolist()
    //
    //        progress.update_status("sentiment_analyst_agent", ticker, "Combining signals")
    //        # Combine signals from both sources with weights
    //        insider_weight = 0.3
    //        news_weight = 0.7
    //
    //        # Calculate weighted signal counts
    //        bullish_signals = (
    //            insider_signals.count("bullish") * insider_weight +
    //            news_signals.count("bullish") * news_weight
    //        )
    //        bearish_signals = (
    //            insider_signals.count("bearish") * insider_weight +
    //            news_signals.count("bearish") * news_weight
    //        )
    //
    //        if bullish_signals > bearish_signals:
    //            overall_signal = "bullish"
    //        elif bearish_signals > bullish_signals:
    //            overall_signal = "bearish"
    //        else:
    //            overall_signal = "neutral"
    //
    //        # Calculate confidence level based on the weighted proportion
    //        total_weighted_signals = len(insider_signals) * insider_weight + len(news_signals) *
    // news_weight
    //        confidence = 0  # Default confidence when there are no signals
    //        if total_weighted_signals > 0:
    //            confidence = round((max(bullish_signals, bearish_signals) /
    // total_weighted_signals) * 100, 2)
    //        reasoning = f"Weighted Bullish signals: {bullish_signals:.1f}, Weighted Bearish
    // signals: {bearish_signals:.1f}"
    //
    //        sentiment_analysis[ticker] = {
    //            "signal": overall_signal,
    //            "confidence": confidence,
    //            "reasoning": reasoning,
    //        }
    //
    //        progress.update_status("sentiment_analyst_agent", ticker, "Done",
    // analysis=json.dumps(reasoning, indent=4))
    //
    //    # Create the sentiment message
    //    message = HumanMessage(
    //        content=json.dumps(sentiment_analysis),
    //        name="sentiment_analyst_agent",
    //    )
    //
    //    # Print the reasoning if the flag is set
    //    if state["metadata"]["show_reasoning"]:
    //        show_agent_reasoning(sentiment_analysis, "Sentiment Analysis Agent")
    //
    //    # Add the signal to the analyst_signals list
    //    state["data"]["analyst_signals"]["sentiment_agent"] = sentiment_analysis
    //
    //    progress.update_status("sentiment_analyst_agent", None, "Done")
    //
    //    return {
    //        "messages": [message],
    //        "data": data,
    //    }

    return new AgentSignal(ticker, Signal.neutral, 0.0f, "");
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: {} - {}", AGENT_NAME, ticker, message);
  }
}
