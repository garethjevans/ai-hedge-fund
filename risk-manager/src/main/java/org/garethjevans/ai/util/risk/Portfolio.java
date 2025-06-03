package org.garethjevans.ai.util.risk;

import java.math.BigDecimal;
import java.util.List;

public class Portfolio {

  private final BigDecimal cash;
  private final BigDecimal marginRequirement;
  private final BigDecimal marginUsed;

  public Portfolio(BigDecimal cash, BigDecimal marginRequirement) {
    this.cash = cash;
    this.marginRequirement = marginRequirement;
    this.marginUsed = BigDecimal.ZERO;
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
    return new Position(ticker, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  public List<Position> all() {
    return List.of();
  }

  public record Position(String ticker, BigDecimal longPosition, BigDecimal shortPosition) {}
}
