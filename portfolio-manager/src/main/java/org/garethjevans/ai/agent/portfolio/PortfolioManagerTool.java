package org.garethjevans.ai.agent.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.garethjevans.ai.common.AgentSignal;
import org.garethjevans.ai.util.risk.RiskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class PortfolioManagerTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioManagerTool.class);

  private final RiskManager riskManager;
  private final ObjectMapper objectMapper;

  public PortfolioManagerTool(RiskManager riskManager, ObjectMapper objectMapper) {
    this.riskManager = riskManager;
    this.objectMapper = objectMapper;
  }

  @Tool(
      name = "generate_trading_recommendations",
      description = "Generates trading recommendations from a list of agent signals")
  public List<Recommendation> recommendationList(
      @ToolParam(description = "List of agent signals") List<AgentSignal> agentSignals,
      ToolContext toolContext) {
    // Get position limits, current prices, and signals for every ticker
    Map<String, BigDecimal> positionLimits = new HashMap<>();
    Map<String, BigDecimal> currentPrices = new HashMap<>();
    Map<String, BigDecimal> maxShares = new HashMap<>();

    // signals_by_ticker = {}
    // for ticker in tickers:
    for (AgentSignal agentSignal : agentSignals) {
      String ticker = agentSignal.ticker();
      updateProgress(ticker, "Processing analyst signals");

      // Get position limits and current prices for the ticker
      RiskManager.Analysis riskData = riskManager.analyseRisk(ticker);
      positionLimits.put(ticker, riskData.remainingPositionLimit());
      currentPrices.put(ticker, riskData.currentPrice());

      // Calculate maximum shares allowed based on position limit and price
      if (currentPrices.get(ticker).compareTo(BigDecimal.ZERO) > 0) {
        maxShares.put(
            ticker,
            positionLimits.get(ticker).divide(currentPrices.get(ticker), 2, RoundingMode.HALF_UP));
      } else {
        maxShares.put(ticker, BigDecimal.ZERO);
      }

      // Get signals for the ticker
      //        ticker_signals = {}
      //        for agent, signals in analyst_signals.items():
      //            if agent != "risk_management_agent" and ticker in signals:
      //                ticker_signals[agent] = {"signal": signals[ticker]["signal"], "confidence":
      // signals[ticker]["confidence"]}
      //        signals_by_ticker[ticker] = ticker_signals

      updateProgress(ticker, "Generating trading decisions");
    }
    updateProgress(null, "Generating trading decisions");
    return List.of();
  }

  public String generateSystemMessage() {
    String body =
        """
                You are a portfolio manager making final trading decisions based on multiple tickers.

                Trading Rules:
                - For long positions:
                * Only buy if you have available cash
                * Only sell if you currently hold long shares of that ticker
                * Sell quantity must be ≤ current long position shares
                * Buy quantity must be ≤ max_shares for that ticker

                - For short positions:
                * Only short if you have available margin (position value × margin requirement)
                * Only cover if you currently have short shares of that ticker
                * Cover quantity must be ≤ current short position shares
                * Short quantity must respect margin requirements

                - The max_shares values are pre-calculated to respect position limits
                - Consider both long and short opportunities based on signals
                - Maintain appropriate risk management with both long and short exposure

                Available Actions:
                - "BUY": Open or add to long position
                - "SELL": Close or reduce long position
                - "SHORT": Open or add to short position
                - "COVER": Close or reduce short position
                - "HOLD": No action

                Inputs:
                - signals_by_ticker: dictionary of ticker → signals
                - max_shares: maximum shares allowed per ticker
                - portfolio_cash: current cash in portfolio
                - portfolio_positions: current positions (both long and short)
                - current_prices: current prices for each ticker
                - margin_requirement: current margin requirement for short positions (e.g., 0.5 means 50%)

                - total_margin_used: total margin currently in use
                """;
    LOGGER.info(body);
    return body;
  }

  public String generateUserMessage(List<AgentSignal> agentSignals) {
    PromptTemplate promptTemplate =
        PromptTemplate.builder()
            .renderer(
                StTemplateRenderer.builder()
                    .startDelimiterToken('{')
                    .endDelimiterToken('}')
                    .build())
            .template(
                """
                                Based on the team's analysis, make your trading decisions for each ticker.

                                Here are the signals by ticker:
                                {signals_by_ticker}

                                Current Prices:
                                {current_prices}

                                Maximum Shares Allowed For Purchases:
                                {max_shares}

                                Portfolio Cash: {portfolio_cash}
                                Current Positions: {portfolio_positions}
                                Current Margin Requirement: {margin_requirement}
                                Total Margin Used: {total_margin_used}

                                Output strictly in JSON with the following structure:
                                {{
                                  "decisions": {{
                                    "TICKER1": {{
                                      "action": "BUY/SELL/SHORT/COVER/HOLD",
                                      "quantity": integer,
                                      "confidence": float between 0 and 100,
                                      "reasoning": "string"
                                    }},
                                    "TICKER2": {{
                                      ...
                                    }},
                                    ...
                                  }}
                                }}
                                """)
            .build();

    // {signals_by_ticker}
    //
    //                                Current Prices:
    //                                {current_prices}
    //
    //                                Maximum Shares Allowed For Purchases:
    //                                {max_shares}
    //
    //                                Portfolio Cash: {portfolio_cash}
    //                                Current Positions: {portfolio_positions}
    //                                Current Margin Requirement: {margin_requirement}
    //                                Total Margin Used: {total_margin_used}

    String body =
        promptTemplate.render(
            Map.of(
                "signals_by_ticker",
                toJson(null),
                "current_prices",
                toJson(null),
                "max_shares",
                "",
                "portfolio_cash",
                "",
                "portfolio_positions",
                "",
                "margin_requirement",
                "",
                "total_margin_used",
                ""));
    LOGGER.info(body);
    return body;
  }

  private String toJson(Object in) {
    try {
      return objectMapper.writeValueAsString(in);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: {} - {}", "Portfolio Manager", ticker, message);
  }

  public record Recommendation(
      @JsonProperty("ticker") String ticker,
      @JsonProperty("action") Action action,
      @JsonProperty("confidence") float confidence,
      @JsonProperty("reasoning") String reasoning) {}

  private enum Action {
    BUY,
    SELL,
    SHORT,
    COVER,
    HOLD
  }
}
