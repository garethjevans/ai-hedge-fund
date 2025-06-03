package org.garethjevans.ai.agent.portfolio;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import org.garethjevans.ai.util.risk.Portfolio;
import org.garethjevans.ai.util.risk.RiskManager;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortfolioManagerConfiguration {

  @Bean
  public PortfolioManagerTool portfolioManagerTool(
      RiskManager riskManager, ObjectMapper objectMapper, Portfolio portfolio) {
    return new PortfolioManagerTool(riskManager, objectMapper, portfolio);
  }

  @Bean
  public ToolCallbackProvider toolCallbackProvider(PortfolioManagerTool tool) {
    return MethodToolCallbackProvider.builder().toolObjects(tool).build();
  }

  @Bean
  public List<McpServerFeatures.SyncPromptSpecification> startupPrompt() {
    return List.of(
        new McpServerFeatures.SyncPromptSpecification(
            new McpSchema.Prompt(
                "analyze_tickers",
                "Perform analysis and offer trading recommendations for tickers",
                List.of(
                    new McpSchema.PromptArgument(
                        "tickers", "The tickers to perform analysis for", true))),
            (exchange, request) -> {
              String tickers = request.arguments().get("tickers").toString();
              return new McpSchema.GetPromptResult(
                  "",
                  List.of(
                      new McpSchema.PromptMessage(
                          McpSchema.Role.USER,
                          new McpSchema.TextContent(
                              "Use all provided AI Stock Agents to perform stock analysis for the following tickers:\n\n"
                                  + tickers
                                  + "\n\nThen generate trading recommendations based on the responses."))));
            }));
  }
}
