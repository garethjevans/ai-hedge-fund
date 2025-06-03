package org.garethjevans.ai.util.risk;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public interface RiskManager {

  Analysis analyseRisk(String ticker);

  record Analysis(
      @JsonProperty("ticker") String ticker,
      @JsonProperty("remaining_position_limit") BigDecimal remainingPositionLimit,
      @JsonProperty("current_price") BigDecimal currentPrice,
      @JsonProperty("reasoning") Reasoning reasoning) {}

  record Reasoning(
      @JsonProperty("portfolio_value") BigDecimal portfolioValue,
      @JsonProperty("current_position_value") BigDecimal currentPositionValue,
      @JsonProperty("position_limit") BigDecimal positionLimit,
      @JsonProperty("remaining_limit") BigDecimal remainingLimit,
      @JsonProperty("available_cash") BigDecimal availableCash) {}

  record PriceDataFrame(
      @JsonProperty("open") List<BigDecimal> open,
      @JsonProperty("close") List<BigDecimal> close,
      @JsonProperty("high") List<BigDecimal> high,
      @JsonProperty("low") List<BigDecimal> low,
      @JsonProperty("volume") List<BigDecimal> volume) {}
}
