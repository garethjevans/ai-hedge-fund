package org.garethjevans.ai.fd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FinancialDatasetsServiceTests {

  @Autowired private FinancialDatasetsService financialDatasetsService;

  @ParameterizedTest
  @ValueSource(strings = {"AAPL", "MSFT", "GOOGL"})
  void canQueryFacts(String ticker) {
    assertThat(financialDatasetsService).isNotNull();

    Facts facts = financialDatasetsService.companyFacts(ticker);
    assertThat(facts).isNotNull();
    assertThat(facts.name()).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAPL", "MSFT", "GOOGL"})
  void canQueryMetrics(String ticker) {
    assertThat(financialDatasetsService).isNotNull();

    List<Metrics> metrics =
        financialDatasetsService.getFinancialMetrics(ticker, LocalDate.now(), Period.ttm, 5);
    assertThat(metrics).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAPL", "MSFT", "GOOGL"})
  void canQueryLineItems(String ticker) {
    assertThat(financialDatasetsService).isNotNull();

    List<LineItem> lineItems =
        financialDatasetsService.searchLineItems(
            ticker,
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
      assertThat(lineItem.ticker()).isEqualTo(ticker);
      assertThat(lineItem.get("total_assets")).isNotNull();
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAPL", "MSFT", "GOOGL"})
  void canQueryInsiderTrades(String ticker) {
    assertThat(financialDatasetsService).isNotNull();

    List<InsiderTrade> trades =
        financialDatasetsService.getInsiderTrades(
            ticker, LocalDate.now().minusYears(1), LocalDate.now(), 1000);
    assertThat(trades).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAPL", "MSFT", "GOOGL"})
  void canQueryCompanyNews(String ticker) {
    assertThat(financialDatasetsService).isNotNull();

    List<CompanyNews> news =
        financialDatasetsService.getCompanyNews(
            ticker, LocalDate.now().minusYears(1), LocalDate.now(), 1000);
    assertThat(news).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAPL", "MSFT", "GOOGL"})
  void canQueryPrices(String ticker) {
    assertThat(financialDatasetsService).isNotNull();

    List<Price> prices =
        financialDatasetsService.getPrices(ticker, LocalDate.now().minusYears(1), LocalDate.now());
    assertThat(prices).isNotNull();
  }
}
