package org.garethjevans.ai.agent.portfolio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.garethjevans.ai.util.risk.RiskManager;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortfolioManagerConfiguration {

  @Bean
  public PortfolioManagerTool portfolioManagerTool(
      RiskManager riskManager, ObjectMapper objectMapper) {
    return new PortfolioManagerTool(riskManager, objectMapper);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(PortfolioManagerTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }
}
