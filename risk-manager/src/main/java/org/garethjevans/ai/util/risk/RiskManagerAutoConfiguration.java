package org.garethjevans.ai.util.risk;

import java.math.BigDecimal;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RiskManagerAutoConfiguration {

  @Bean
  public Portfolio portfolio(
      @Value("${portfolio.cash:100000.0}") BigDecimal cash,
      @Value("${portfolio.margin-requirement:0.0}") BigDecimal marginRequirement) {
    return new Portfolio(cash, marginRequirement);
  }

  @Bean
  public RiskManager riskManager(
      FinancialDatasetsService financialDatasetsService, Portfolio portfolio) {
    return new DefaultRiskManager(financialDatasetsService, portfolio);
  }
}
