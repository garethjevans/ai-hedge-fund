package org.garethjevans.ai.agent.warrenbuffett;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentWarrenBuffettConfiguration {

  @Bean
  public AgentWarrenBuffettTool agentWarrenBuffet(
      FinancialDatasetsService service, ObjectMapper objectMapper) {
    return new AgentWarrenBuffettTool(service, objectMapper);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentWarrenBuffettTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
