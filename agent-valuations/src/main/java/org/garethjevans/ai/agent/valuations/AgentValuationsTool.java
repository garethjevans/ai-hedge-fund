package org.garethjevans.ai.agent.valuations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.garethjevans.ai.common.AgentSignal;
import org.garethjevans.ai.common.Signal;
import org.garethjevans.ai.fd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AgentValuationsTool {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentValuationsTool.class);

  private static final String AGENT_NAME = "Valuations Agent";

  private final FinancialDatasetsService financialDatasets;

  public AgentValuationsTool(FinancialDatasetsService financialDatasets) {
    this.financialDatasets = financialDatasets;
  }

  @Tool(
      name = "valuations_analysis",
      description = "Performs stock analysis using Valuations methods by ticker")
  public AgentSignal performAnalysisForTicker(
      @ToolParam(description = "Ticker to perform analysis for") String ticker,
      ToolContext toolContext) {
    LOGGER.info("Analyzes stocks using Valuations principles.");

    // def valuation_analyst_agent(state: AgentState):
    //    """Run valuation across tickers and write signals back to `state`."""
    //
    //    data = state["data"]
    LocalDate endDate = LocalDate.now();
    //
    //    valuation_analysis: dict[str, dict] = {}
    //
    //    for ticker in tickers:
    updateProgress(ticker, "Fetching financial data");
    //
    // Historical financial metrics (pull 8 latest TTM snapshots for medians) ---
    List<Metrics> metrics = financialDatasets.getFinancialMetrics(ticker, endDate, Period.ttm, 8);

    if (metrics.isEmpty()) {
      updateProgress(ticker, "Failed: No financial metrics found");
      return null;
    }

    Metrics mostRecentMetric = metrics.get(0);
    // --- Fine‑grained line‑items (need two periods to calc WC change) ---
    updateProgress(ticker, "Gathering line items");

    List<LineItem> lineItems =
        financialDatasets.searchLineItems(
            ticker,
            endDate,
            List.of(
                "free_cash_flow",
                "net_income",
                "depreciation_and_amortization",
                "capital_expenditure",
                "working_capital"),
            Period.ttm,
            2);

    if (lineItems.size() < 2) {
      updateProgress(ticker, "Failed: Insufficient financial line items");
      return null;
    }

    LineItem liCurrent = lineItems.get(0);
    LineItem liPrevious = lineItems.get(1);

    // ------------------------------------------------------------------
    // Valuation models
    // ------------------------------------------------------------------
    BigDecimal workingCapitalChange =
        liCurrent.get("working_capital").subtract(liPrevious.get("working_capital"));

    //
    // Owner Earnings
    BigDecimal ownerValue =
        calculateOwnerEarningsValue(
            liCurrent.get("net_income"),
            liCurrent.get("depreciation_and_amortization"),
            liCurrent.get("capital_expenditure"),
            workingCapitalChange,
            mostRecentMetric.earningsGrowth() == null
                ? new BigDecimal("0.05")
                : mostRecentMetric.earningsGrowth());

    //        owner_val = calculate_owner_earnings_value(
    //            net_income=li_curr.net_income,
    //            depreciation=li_curr.depreciation_and_amortization,
    //            capex=li_curr.capital_expenditure,
    //            working_capital_change=wc_change,
    //            growth_rate=most_recent_metrics.earnings_growth or 0.05,
    //        )
    //
    // Discounted Cash Flow
    //        dcf_val = calculate_intrinsic_value(
    //            free_cash_flow=li_curr.free_cash_flow,
    //            growth_rate=most_recent_metrics.earnings_growth or 0.05,
    //            discount_rate=0.10,
    //            terminal_growth_rate=0.03,
    //            num_years=5,
    //        )
    //
    // Implied Equity Value
    //        ev_ebitda_val = calculate_ev_ebitda_value(financial_metrics)
    //
    // Residual Income Model
    //        rim_val = calculate_residual_income_value(
    //            market_cap=most_recent_metrics.market_cap,
    //            net_income=li_curr.net_income,
    //            price_to_book_ratio=most_recent_metrics.price_to_book_ratio,
    //            book_value_growth=most_recent_metrics.book_value_growth or 0.03,
    //        )
    //
    // ------------------------------------------------------------------
    // Aggregate & signal
    // ------------------------------------------------------------------
    //        market_cap = get_market_cap(ticker, end_date)
    BigDecimal marketCap = financialDatasets.getMarketCap(ticker, endDate);

    //        if not market_cap:
    updateProgress(ticker, "Failed: Market cap unavailable");
    //            continue
    //
    //        method_values = {
    //            "dcf": {"value": dcf_val, "weight": 0.35},
    //            "owner_earnings": {"value": owner_val, "weight": 0.35},
    //            "ev_ebitda": {"value": ev_ebitda_val, "weight": 0.20},
    //            "residual_income": {"value": rim_val, "weight": 0.10},
    //        }
    //
    //        total_weight = sum(v["weight"] for v in method_values.values() if v["value"] > 0)
    //        if total_weight == 0:
    //            progress.update_status("valuation_analyst_agent", ticker, "Failed: All valuation
    // methods zero")
    //            continue
    //
    //        for v in method_values.values():
    //            v["gap"] = (v["value"] - market_cap) / market_cap if v["value"] > 0 else None
    //
    //        weighted_gap = sum(
    //            v["weight"] * v["gap"] for v in method_values.values() if v["gap"] is not None
    //        ) / total_weight
    //
    //        signal = "bullish" if weighted_gap > 0.15 else "bearish" if weighted_gap < -0.15 else
    // "neutral"
    //        confidence = round(min(abs(weighted_gap) / 0.30 * 100, 100))
    //
    //        reasoning = {
    //            f"{m}_analysis": {
    //                "signal": (
    //                    "bullish" if vals["gap"] and vals["gap"] > 0.15 else
    //                    "bearish" if vals["gap"] and vals["gap"] < -0.15 else "neutral"
    //                ),
    //                "details": (
    //                    f"Value: ${vals['value']:,.2f}, Market Cap: ${market_cap:,.2f}, "
    //                    f"Gap: {vals['gap']:.1%}, Weight: {vals['weight']*100:.0f}%"
    //                ),
    //            }
    //            for m, vals in method_values.items() if vals["value"] > 0
    //        }
    //
    //        valuation_analysis[ticker] = {
    //            "signal": signal,
    //            "confidence": confidence,
    //            "reasoning": reasoning,
    //        }
    updateProgress(ticker, "Done");

    // #############################
    // # Helper Valuation Functions
    // #############################
    //

    //
    //
    // def calculate_intrinsic_value(
    //    free_cash_flow: float | None,
    //    growth_rate: float = 0.05,
    //    discount_rate: float = 0.10,
    //    terminal_growth_rate: float = 0.02,
    //    num_years: int = 5,
    // ) -> float:
    //    """Classic DCF on FCF with constant growth and terminal value."""
    //    if free_cash_flow is None or free_cash_flow <= 0:
    //        return 0
    //
    //    pv = 0.0
    //    for yr in range(1, num_years + 1):
    //        fcft = free_cash_flow * (1 + growth_rate) ** yr
    //        pv += fcft / (1 + discount_rate) ** yr
    //
    //    term_val = (
    //        free_cash_flow * (1 + growth_rate) ** num_years * (1 + terminal_growth_rate)
    //    ) / (discount_rate - terminal_growth_rate)
    //    pv_term = term_val / (1 + discount_rate) ** num_years
    //
    //    return pv + pv_term
    //
    //
    // def calculate_ev_ebitda_value(financial_metrics: list):
    //    """Implied equity value via median EV/EBITDA multiple."""
    //    if not financial_metrics:
    //        return 0
    //    m0 = financial_metrics[0]
    //    if not (m0.enterprise_value and m0.enterprise_value_to_ebitda_ratio):
    //        return 0
    //    if m0.enterprise_value_to_ebitda_ratio == 0:
    //        return 0
    //
    //    ebitda_now = m0.enterprise_value / m0.enterprise_value_to_ebitda_ratio
    //    med_mult = median([
    //        m.enterprise_value_to_ebitda_ratio for m in financial_metrics if
    // m.enterprise_value_to_ebitda_ratio
    //    ])
    //    ev_implied = med_mult * ebitda_now
    //    net_debt = (m0.enterprise_value or 0) - (m0.market_cap or 0)
    //    return max(ev_implied - net_debt, 0)
    //
    //
    // def calculate_residual_income_value(
    //    market_cap: float | None,
    //    net_income: float | None,
    //    price_to_book_ratio: float | None,
    //    book_value_growth: float = 0.03,
    //    cost_of_equity: float = 0.10,
    //    terminal_growth_rate: float = 0.03,
    //    num_years: int = 5,
    // ):
    //    """Residual Income Model (Edwards‑Bell‑Ohlson)."""
    //    if not (market_cap and net_income and price_to_book_ratio and price_to_book_ratio > 0):
    //        return 0
    //
    //    book_val = market_cap / price_to_book_ratio
    //    ri0 = net_income - cost_of_equity * book_val
    //    if ri0 <= 0:
    //        return 0
    //
    //    pv_ri = 0.0
    //    for yr in range(1, num_years + 1):
    //        ri_t = ri0 * (1 + book_value_growth) ** yr
    //        pv_ri += ri_t / (1 + cost_of_equity) ** yr
    //
    //    term_ri = ri0 * (1 + book_value_growth) ** (num_years + 1) / (
    //        cost_of_equity - terminal_growth_rate
    //    )
    //    pv_term = term_ri / (1 + cost_of_equity) ** num_years
    //
    //    intrinsic = book_val + pv_ri + pv_term
    //    return intrinsic * 0.8  # 20% margin of safety

    return new AgentSignal(AGENT_NAME, ticker, Signal.neutral, 0.0f, "");
  }

  /** Buffett owner‑earnings valuation with margin‑of‑safety. */
  private BigDecimal calculateOwnerEarningsValue(
      BigDecimal netIncome,
      BigDecimal depreciationAndAmortization,
      BigDecimal capitalExpenditure,
      BigDecimal workingCapitalChange,
      BigDecimal growthRate) {
    BigDecimal requiredReturn = new BigDecimal("0.15");
    BigDecimal marginOfSafety = new BigDecimal("0.25");
    int numYears = 5;

    // def calculate_owner_earnings_value(
    //    net_income: float | None,
    //    depreciation: float | None,
    //    capex: float | None,
    //    working_capital_change: float | None,
    //    growth_rate: float = 0.05,
    //    required_return: float = 0.15,
    //    margin_of_safety: float = 0.25,
    //    num_years: int = 5,
    // ) -> float:
    //    """"""
    //    if not all(isinstance(x, (int, float)) for x in [net_income, depreciation, capex,
    // working_capital_change]):
    //        return 0

    BigDecimal ownerEarnings =
        netIncome
            .add(depreciationAndAmortization)
            .subtract(capitalExpenditure)
            .subtract(workingCapitalChange);
    if (ownerEarnings.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    //
    BigDecimal pv = BigDecimal.ZERO;
    //    for yr in range(1, num_years + 1):
    for (int yr = 1; yr <= numYears; yr++) {
      // future = owner_earnings * (1 + growth_rate) ** yr
      BigDecimal future = ownerEarnings.multiply((BigDecimal.ONE.add(growthRate).pow(yr)));
      // pv += future / (1 + required_return) ** yr
      pv =
          pv.add(
              future.divide(BigDecimal.ONE.add(requiredReturn).pow(yr), 2, RoundingMode.HALF_UP));
    }

    BigDecimal terminalGrowth = growthRate.min(new BigDecimal("0.03"));

    //    terminal_growth = min(growth_rate, 0.03)
    //    term_val = (owner_earnings * (1 + growth_rate) ** num_years * (1 + terminal_growth)) / (
    //        required_return - terminal_growth
    //    )
    //    pv_term = term_val / (1 + required_return) ** num_years
    //
    //    intrinsic = pv + pv_term
    //    return intrinsic * (1 - margin_of_safety)

    return BigDecimal.ZERO;
  }

  private void updateProgress(String ticker, String message) {
    LOGGER.info("{}: {} - {}", AGENT_NAME, ticker, message);
  }
}
