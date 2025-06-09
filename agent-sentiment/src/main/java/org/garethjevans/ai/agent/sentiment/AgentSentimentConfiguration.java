package org.garethjevans.ai.agent.sentiment;

import org.garethjevans.ai.fd.FinancialDatasetsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentSentimentConfiguration {

  @Bean
  public AgentSentimentTool agentSentiment(FinancialDatasetsService service) {
    return new AgentSentimentTool(service);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(AgentSentimentTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
