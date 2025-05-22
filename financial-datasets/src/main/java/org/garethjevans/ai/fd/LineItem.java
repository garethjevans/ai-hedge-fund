package org.garethjevans.ai.fd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class LineItem {

  private final Map<String, Object> data;

  LineItem(Map<String, Object> data) {
    this.data = data;
  }

  public String ticker() {
    return (String) data.get("ticker");
  }

  public LocalDate reportPeriod() {
    return LocalDate.parse(data.get("report_period").toString());
  }

  public Period period() {
    return Period.valueOf(data.get("period").toString());
  }

  public String currency() {
    return (String) data.get("currency");
  }

  public BigDecimal get(String name) {
    if (data.containsKey(name)) {
      if (data.get(name) == null) {
        return null;
      }
      return new BigDecimal(data.get(name).toString());
    }
    return null;
  }

  // ,"report_period":"2025-03-29","period":"ttm","currency":"USD"
}
