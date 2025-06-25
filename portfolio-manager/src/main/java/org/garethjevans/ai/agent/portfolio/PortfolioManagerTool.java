package org.garethjevans.ai.agent.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.garethjevans.ai.common.AgentSignal;
import org.garethjevans.ai.common.Signal;
import org.garethjevans.ai.util.risk.Portfolio;
import org.garethjevans.ai.util.risk.RiskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class PortfolioManagerTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioManagerTool.class);

  private final RiskManager riskManager;
  private final ObjectMapper objectMapper;
  private final Portfolio portfolio;

  public PortfolioManagerTool(
      RiskManager riskManager, ObjectMapper objectMapper, Portfolio portfolio) {
    this.riskManager = riskManager;
    this.objectMapper = objectMapper;
    this.portfolio = portfolio;
  }

  @Tool(name = "current_portfolio", description = "Returns the current portfolio")
  public Portfolio currentPortfolio(ToolContext toolContext) {
    LOGGER.info("returning current portfolio {}", portfolio);
    return portfolio;
  }

  @Tool(
      name = "current_price_for_ticker",
      description = "Returns the current price for this ticker")
  public BigDecimal currentPrice(
      @ToolParam(description = "The ticker to return the price for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("returning current price for {}", ticker);

    RiskManager.Analysis riskData = riskManager.analyseRisk(ticker);

    return riskData.currentPrice();
  }

  @Tool(
      name = "generate_trading_recommendations",
      description = "Generates trading recommendations from a list of agent signals")
  public TradingRecommendations recommendationList(
      @ToolParam(description = "List of agent signals") List<AgentSignal> agentSignals,
      ToolContext toolContext) {

    // Get position limits, current prices, and signals for every ticker
    Map<String, BigDecimal> positionLimits = new HashMap<>();
    Map<String, BigDecimal> currentPrices = new HashMap<>();
    Map<String, BigDecimal> maxShares = new HashMap<>();
    Map<String, Map<String, PortfolioSignal>> signalsByTicker = new HashMap<>();

    // signals_by_ticker = {}
    // for ticker in tickers:
    for (AgentSignal agentSignal : agentSignals) {
      String ticker = agentSignal.ticker();
      updateProgress(ticker, "Processing analyst signals");

      // Get position limits and current prices for the ticker
      RiskManager.Analysis riskData = riskManager.analyseRisk(ticker);
      BigDecimal positionLimit = riskData.remainingPositionLimit();
      BigDecimal currentPrice = riskData.currentPrice();

      positionLimits.put(ticker, positionLimit);
      currentPrices.put(ticker, currentPrice);

      // Calculate maximum shares allowed based on position limit and price
      if (currentPrice.compareTo(BigDecimal.ZERO) > 0) {
        maxShares.put(ticker, positionLimit.divide(currentPrice, 2, RoundingMode.HALF_UP));
      } else {
        maxShares.put(ticker, BigDecimal.ZERO);
      }

      if (!signalsByTicker.containsKey(ticker)) {
        signalsByTicker.put(ticker, new HashMap<>());
      }

      signalsByTicker
          .get(ticker)
          .put(
              agentSignal.agent(),
              new PortfolioSignal(agentSignal.signal(), agentSignal.confidence()));

      updateProgress(ticker, "Generating trading decisions");
    }

    updateProgress(null, "Generating trading decisions");

    TradingRecommendations recommendations =
        generateOutput(signalsByTicker, currentPrices, maxShares, portfolio, toolContext);

    updateProgress(null, "Done");

    return recommendations;
  }

  private TradingRecommendations generateOutput(
      Map<String, Map<String, PortfolioSignal>> signalsByTicker,
      Map<String, BigDecimal> currentPrices,
      Map<String, BigDecimal> maxShares,
      Portfolio portfolio,
      ToolContext toolContext) {
    try {
      StringBuilder tradingSignalOutput = new StringBuilder();

      McpToolUtils.getMcpExchange(toolContext)
          .ifPresent(
              exchange -> {
                exchange.loggingNotification(
                    McpSchema.LoggingMessageNotification.builder()
                        .level(McpSchema.LoggingLevel.INFO)
                        .data("Calculating Trading Signal")
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
                                          generateUserMessage(
                                              signalsByTicker,
                                              currentPrices,
                                              maxShares,
                                              portfolio)))));

                  var llmMessageRequest =
                      messageRequestBuilder
                          // .modelPreferences(
                          // McpSchema.ModelPreferences.builder().addHint("gpt-4o").build())
                          .build();
                  McpSchema.CreateMessageResult llmResponse =
                      exchange.createMessage(llmMessageRequest);

                  tradingSignalOutput.append(
                      ((McpSchema.TextContent) llmResponse.content()).text());
                }

                exchange.loggingNotification(
                    McpSchema.LoggingMessageNotification.builder()
                        .level(McpSchema.LoggingLevel.INFO)
                        .data("Completed Trading Signal")
                        .build());
              });

      String withoutMarkdown = removeMarkdown(tradingSignalOutput.toString());
      LOGGER.info("Got sampling response '{}'", withoutMarkdown);

      return objectMapper.readValue(withoutMarkdown, TradingRecommendations.class);
    } catch (Exception e) {
      LOGGER.warn("Error in analysis, returning no recommendations", e);
      return new TradingRecommendations(Map.of());
    }
  }

  private String removeMarkdown(String in) {
    return in.replace("```json", "").replace("```", "").trim();
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

  public String generateUserMessage(
      Map<String, Map<String, PortfolioSignal>> signalsByTicker,
      Map<String, BigDecimal> currentPrices,
      Map<String, BigDecimal> maxShares,
      Portfolio portfolio) {
    PromptTemplate promptTemplate =
        PromptTemplate.builder()
            .renderer(
                StTemplateRenderer.builder()
                    .startDelimiterToken('<')
                    .endDelimiterToken('>')
                    .build())
            .template(
                """
                                Based on the team's analysis, make your trading decisions for each ticker.

                                Here are the signals by ticker:
                                <signals_by_ticker>

                                Current Prices:
                                <current_prices>

                                Maximum Shares Allowed For Purchases:
                                <max_shares>

                                Portfolio Cash: <portfolio_cash>
                                Current Positions: <portfolio_positions>
                                Current Margin Requirement: <margin_requirement>
                                Total Margin Used: <total_margin_used>

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

    String body =
        promptTemplate.render(
            Map.of(
                "signals_by_ticker",
                toJson(signalsByTicker),
                "current_prices",
                toJson(currentPrices),
                "max_shares",
                toJson(maxShares),
                "portfolio_cash",
                portfolio.cash().toString(),
                "portfolio_positions",
                toJson(portfolio.positions()),
                "margin_requirement",
                portfolio.marginRequirement().toString(),
                "total_margin_used",
                portfolio.marginUsed().toString()));
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

  private enum Action {
    BUY,
    SELL,
    SHORT,
    COVER,
    HOLD
  }

  public record PortfolioSignal(
      @JsonProperty("signal") Signal signal, @JsonProperty("confidence") float confidence) {}

  public record Recommendation(
      @JsonProperty("ticker") String ticker,
      @JsonProperty("action") Action action,
      @JsonProperty("confidence") float confidence,
      @JsonProperty("reasoning") String reasoning) {}

  public record TradingRecommendations(
      @JsonProperty("decisions") Map<String, Recommendation> decisions) {}
}
