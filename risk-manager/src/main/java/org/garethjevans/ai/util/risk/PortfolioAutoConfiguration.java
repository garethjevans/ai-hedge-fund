package org.garethjevans.ai.util.risk;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(PortfolioConfigurationProperties.class)
public class PortfolioAutoConfiguration {

  @Bean
  public Portfolio portfolio(
      @Value("${portfolio.cash:100000.0}") BigDecimal cash,
      @Value("${portfolio.margin-requirement:0.0}") BigDecimal marginRequirement,
      PortfolioConfigurationProperties props) {
    return new Portfolio(cash, marginRequirement, props.getPositions());
  }
}
