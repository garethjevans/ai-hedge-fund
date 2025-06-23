package org.garethjevans.ai.agent.technical;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentTechnicalConfiguration {

  @Bean
  public AgentTechnicalTool agentTechnical(FinancialDatasetsService service) {
    return new AgentTechnicalTool(service);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentTechnicalTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
