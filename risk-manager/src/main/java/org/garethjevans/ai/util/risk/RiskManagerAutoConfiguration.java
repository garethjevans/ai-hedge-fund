package org.garethjevans.ai.util.risk;

import java.math.BigDecimal;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RiskManagerAutoConfiguration {

  @Bean
  public Portfolio portfolio(@Value("${portfolio.cash:10000.0}") BigDecimal cash) {
    return new Portfolio(cash);
  }

  @Bean
  public RiskManager riskManager(
      FinancialDatasetsService financialDatasetsService, Portfolio portfolio) {
    return new DefaultRiskManager(financialDatasetsService, portfolio);
  }
}
