package org.garethjevans.ai.util.risk;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("portfolio")
public class PortfolioConfigurationProperties {

  private List<Portfolio.Position> positions;

  public List<Portfolio.Position> getPositions() {
    return positions;
  }

  public void setPositions(List<Portfolio.Position> positions) {
    this.positions = positions;
  }
}
