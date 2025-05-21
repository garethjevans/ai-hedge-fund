package org.garethjevans.ai.fd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FinancialDatasetsServiceTests {

  @Autowired private FinancialDatasetsService financialDatasetsService;

  @Test
  void canQueryAppleFacts() {
    assertThat(financialDatasetsService).isNotNull();

    FinancialDatasetsService.CompanyFacts facts = financialDatasetsService.companyFacts("AAPL");
    assertThat(facts).isNotNull();
    assertThat(facts.name()).isEqualTo("Apple Inc");
  }

  @Test
  void canQueryAppleMetrics() {
    assertThat(financialDatasetsService).isNotNull();

    List<FinancialDatasetsService.Metric> metrics =
        financialDatasetsService.getFinancialMetrics("AAPL", LocalDate.now(), Period.ttm, 5);
    assertThat(metrics).isNotNull();
  }

  @Test
  void canQueryAppleLineItems() {
    assertThat(financialDatasetsService).isNotNull();

    List<LineItem> lineItems =
        financialDatasetsService.searchLineItems(
            "AAPL",
            LocalDate.now(),
            List.of(
                "capital_expenditure",
                "depreciation_and_amortization",
                "net_income",
                "outstanding_shares",
                "total_assets",
                "total_liabilities",
                "dividends_and_other_cash_distributions",
                "issuance_or_purchase_of_equity_shares"),
            Period.ttm,
            10);
    assertThat(lineItems).isNotNull();

    for (LineItem lineItem : lineItems) {
      assertThat(lineItem.ticker()).isEqualTo("AAPL");
      assertThat(lineItem.get("total_assets")).isNotNull();
    }
  }
}
