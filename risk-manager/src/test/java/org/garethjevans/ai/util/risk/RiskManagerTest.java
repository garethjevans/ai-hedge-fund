package org.garethjevans.ai.util.risk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RiskManagerTest {

  @Autowired private RiskManager riskManager;

  @Test
  public void canAnalyseRisk() {
    assertThat(riskManager).isNotNull();

    var analysis = riskManager.analyseRisk("AVGO");
    assertThat(analysis).isNotNull();
  }
}
