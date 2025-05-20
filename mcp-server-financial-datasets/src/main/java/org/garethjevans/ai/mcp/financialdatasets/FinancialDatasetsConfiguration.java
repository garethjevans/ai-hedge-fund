package org.garethjevans.ai.mcp.financialdatasets;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class FinancialDatasetsConfiguration {

    @Bean
    public List<ToolCallback> weatherTools(FinancialDatasetsTool financialDatasetsTool) {
        return List.of(ToolCallbacks.from(financialDatasetsTool));
    }

    @Bean
    public FinancialDatasetsTool financialDatasetsTool(RestClient.Builder builder,
                                                       @Value("${financial.datasets.url}") String url,
                                                       @Value("${financial.datasets.api-key}") String apiKey) {
        return new FinancialDatasetsTool(builder, url, apiKey);
    }
}
