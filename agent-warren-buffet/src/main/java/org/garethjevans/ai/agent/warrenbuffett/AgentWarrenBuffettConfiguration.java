package org.garethjevans.ai.agent.warrenbuffett;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentWarrenBuffettConfiguration {

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentWarrenBuffettTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }

  @Bean
  public AgentWarrenBuffettTool agentWarrenBuffet(FinancialDatasetsService service) {
    return new AgentWarrenBuffettTool(service);
  }
}
