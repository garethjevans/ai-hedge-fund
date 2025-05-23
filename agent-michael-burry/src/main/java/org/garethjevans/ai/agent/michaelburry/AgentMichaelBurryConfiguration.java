package org.garethjevans.ai.agent.michaelburry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentMichaelBurryConfiguration {

  @Bean
  public AgentMichaelBurryTool agentMichaelBurry(
      FinancialDatasetsService service, ObjectMapper objectMapper) {
    return new AgentMichaelBurryTool(service, objectMapper);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentMichaelBurryTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
