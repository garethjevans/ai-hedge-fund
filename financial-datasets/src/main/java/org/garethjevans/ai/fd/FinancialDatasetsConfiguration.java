package org.garethjevans.ai.fd;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FinancialDatasetsConfiguration {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Bean
  public FinancialDatasetsService financialDatasetsService(
      RestClient.Builder builder,
      @Value("${financial.datasets.url}") String url,
      @Value("${financial.datasets.api-key}") String apiKey) {
    return new FinancialDatasetsService(builder, url, apiKey);
  }
}
