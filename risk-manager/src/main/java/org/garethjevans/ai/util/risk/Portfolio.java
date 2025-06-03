package org.garethjevans.ai.util.risk;

import java.math.BigDecimal;
import java.util.List;

public class Portfolio {

  private final BigDecimal cash;

  public Portfolio(BigDecimal cash) {
    this.cash = cash;
  }

  public BigDecimal cash() {
    return cash;
  }

  public Position position(String ticker) {
    return new Position(ticker, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  public List<Position> all() {
    return List.of();
  }

  public record Position(String ticker, BigDecimal longPosition, BigDecimal shortPosition) {}
}
