package org.garethjevans.ai.util.risk;

import java.math.BigDecimal;
import java.util.List;

public class Portfolio {

  private final BigDecimal cash;
  private final BigDecimal marginRequirement;
  private final BigDecimal marginUsed;
  private final List<Position> positions;

  public Portfolio(BigDecimal cash, BigDecimal marginRequirement, List<Position> positions) {
    this.cash = cash;
    this.marginRequirement = marginRequirement;
    this.marginUsed = BigDecimal.ZERO;
    this.positions = positions;
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

  public Position position(String ticker) {
    return positions.stream().filter(p -> p.ticker.equals(ticker)).findFirst().orElse(null);
  }

  public List<Position> all() {
    return positions;
  }

  public record Position(String ticker, BigDecimal longPosition, BigDecimal shortPosition) {}
}
