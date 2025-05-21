package org.garethjevans.ai.fd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ToolTests {

  @Autowired private FinancialDatasetsService financialDatasetsService;

  @Test
  void canQueryAppleTicker() {
    assertThat(financialDatasetsService).isNotNull();

    FinancialDatasetsService.CompanyFacts facts = financialDatasetsService.companyFacts("AAPL");
    assertThat(facts).isNotNull();
    assertThat(facts.name()).isEqualTo("Apple Inc");
  }
}
