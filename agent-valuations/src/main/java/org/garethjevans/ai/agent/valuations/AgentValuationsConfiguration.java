package org.garethjevans.ai.agent.valuations;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentValuationsConfiguration {

  @Bean
  public AgentValuationsTool agentValuations(FinancialDatasetsService service) {
    return new AgentValuationsTool(service);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentValuationsTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
