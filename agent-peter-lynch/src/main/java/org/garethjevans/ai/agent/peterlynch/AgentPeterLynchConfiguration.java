package org.garethjevans.ai.agent.peterlynch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentPeterLynchConfiguration {

  @Bean
  public AgentPeterLynchTool agentPeterLynch(
      FinancialDatasetsService service, ObjectMapper objectMapper) {
    return new AgentPeterLynchTool(service, objectMapper);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentPeterLynchTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
