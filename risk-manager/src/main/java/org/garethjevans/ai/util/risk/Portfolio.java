package org.garethjevans.ai.util.risk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Portfolio {

  private static final Logger LOGGER = LoggerFactory.getLogger(Portfolio.class);

  private final BigDecimal cash;
  private final BigDecimal marginRequirement;
  private final BigDecimal marginUsed;
  private final List<Position> positions;

  public Portfolio(BigDecimal cash, BigDecimal marginRequirement, List<Position> positions) {
    this.cash = cash;
    this.marginRequirement = marginRequirement;
    this.marginUsed = BigDecimal.ZERO;
    this.positions = positions;
    LOGGER.info("Portfolio created with positions {}", positions);
  }

  public BigDecimal cash() {
    return cash;
  }

  public BigDecimal marginRequirement() {
    return marginRequirement;
  }

  public BigDecimal marginUsed() {
    return marginUsed;
  }

  @JsonIgnore
  public Position position(String ticker) {
    return positions().stream()
        .filter(p -> p.ticker.equals(ticker))
        .findFirst()
        .orElse(new Position(ticker, BigDecimal.ZERO, BigDecimal.ZERO));
  }

  public List<Position> positions() {
    if (positions == null) {
      return List.of();
    }
    return positions;
  }

  public record Position(String ticker, BigDecimal longPosition, BigDecimal shortPosition) {}
}
