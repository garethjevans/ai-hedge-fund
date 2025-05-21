package org.garethjevans.ai.agent.warrenbuffet;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentWarrenBuffetConfiguration {

  @Bean
  public AgentWarrenBuffetTool agentWarrenBuffet(FinancialDatasetsService service) {
    return new AgentWarrenBuffetTool(service);
  }
}
