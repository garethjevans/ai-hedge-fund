package org.garethjevans.ai.util.risk;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RiskManagerAutoConfiguration {

  @Bean
  public RiskManager riskManager(
      FinancialDatasetsService financialDatasetsService, Portfolio portfolio) {
    return new DefaultRiskManager(financialDatasetsService, portfolio);
  }
}
