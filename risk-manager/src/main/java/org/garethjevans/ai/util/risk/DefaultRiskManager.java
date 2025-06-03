package org.garethjevans.ai.util.risk;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.garethjevans.ai.fd.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class DefaultRiskManager implements RiskManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRiskManager.class);

  private final FinancialDatasetsService datasetsService;
  private final Portfolio portfolio;

  public DefaultRiskManager(FinancialDatasetsService datasetsService, Portfolio portfolio) {
    this.datasetsService = datasetsService;
    this.portfolio = portfolio;
  }

  /** Controls position sizing based on real-world risk factors for multiple tickers. */
  @Override
  public Analysis analyseRisk(String ticker) {
    updateProgress(ticker, "Fetching price data");

    LocalDate end = LocalDate.now();
    LocalDate start = end.minusMonths(3);

    var prices = datasetsService.getPrices(ticker, start, end);

    if (prices.isEmpty()) {
      updateProgress(ticker, "Warning: No price data found");
      return null;
    }

    PriceDataFrame priceDataFrame = toDataFrame(prices);

    BigDecimal currentPrice = priceDataFrame.close().getLast();
    updateProgress(ticker, "Current price: " + currentPrice);

    // Calculate total portfolio value based on current market prices (Net Liquidation Value)
    BigDecimal totalPortfolioValue = portfolio.cash();

    List<Portfolio.Position> allPositions = portfolio.all();

    BigDecimal totalShortValue =
        allPositions.stream()
            .map(p -> p.shortPosition().multiply(currentPriceForTicker(p.ticker())))
            .reduce((x, y) -> x.add(y))
            .orElse(BigDecimal.ZERO);

    BigDecimal totalLongValue =
        allPositions.stream()
            .map(p -> p.longPosition().multiply(currentPriceForTicker(p.ticker())))
            .reduce((x, y) -> x.add(y))
            .orElse(BigDecimal.ZERO);

    totalPortfolioValue = totalPortfolioValue.add(totalLongValue).subtract(totalShortValue);

    updateProgress(ticker, "Total portfolio value: " + totalPortfolioValue);

    // Calculate risk limits for ticker
    updateProgress(ticker, "Calculating position limits");

    // Calculate current market value of this position
    Portfolio.Position position = portfolio.position(ticker);
    BigDecimal longValue = position.longPosition().multiply(currentPrice);
    BigDecimal shortValue = position.shortPosition().multiply(currentPrice);

    BigDecimal currentPositionValue = longValue.subtract(shortValue).abs();

    // Calculate position limit (20% of total portfolio)
    BigDecimal positionLimit = totalPortfolioValue.multiply(new BigDecimal("0.20"));

    // Calculate remaining limit for this position
    BigDecimal remainingPositionLimit = positionLimit.subtract(currentPositionValue);
    //
    // Ensure we don't exceed available cash
    BigDecimal maxPositionSize = remainingPositionLimit.min(portfolio.cash());

    var analysis =
        new Analysis(
            ticker,
            maxPositionSize,
            currentPrice,
            new Reasoning(
                totalPortfolioValue,
                currentPositionValue,
                positionLimit,
                remainingPositionLimit,
                portfolio.cash()));

    LOGGER.info("Analysis: {}", analysis);

    updateProgress(ticker, "Done");
    return analysis;
  }

  private BigDecimal currentPriceForTicker(String ticker) {
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(3);

    var prices = datasetsService.getPrices(ticker, start, end);
    PriceDataFrame priceDataFrame = toDataFrame(prices);

    return priceDataFrame.close().getLast();
  }

  private PriceDataFrame toDataFrame(List<Price> prices) {
    return new PriceDataFrame(
        prices.stream().map(Price::open).toList(),
        prices.stream().map(Price::close).toList(),
        prices.stream().map(Price::high).toList(),
        prices.stream().map(Price::low).toList(),
        prices.stream().map(Price::volume).toList());
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: - {}", ticker, message);
  }
}
