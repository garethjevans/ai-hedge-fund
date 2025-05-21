package org.garethjevans.ai.agent.warrenbuffett;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentWarrenBuffettConfiguration {

  @Bean
  public AgentWarrenBuffettTool agentWarrenBuffet(FinancialDatasetsService service) {
    return new AgentWarrenBuffettTool(service);
  }
}
