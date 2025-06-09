package org.garethjevans.ai.agent.fundamentals;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentFundamentalsConfiguration {

  @Bean
  public AgentFundamentalsTool agentFundamentals(FinancialDatasetsService service) {
    return new AgentFundamentalsTool(service);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentFundamentalsTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
