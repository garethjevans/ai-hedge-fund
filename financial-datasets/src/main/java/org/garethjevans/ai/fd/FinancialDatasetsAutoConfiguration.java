package org.garethjevans.ai.fd;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
public class FinancialDatasetsAutoConfiguration {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Bean
  public CacheService cacheService(@Value("${financial.datasets.cache.dir}") String cacheDir) {
    return new CacheService(new File(cacheDir));
  }

  @Bean
  public FinancialDatasetsService financialDatasetsService(
      RestClient.Builder builder,
      @Value("${financial.datasets.url}") String url,
      @Value("${financial.datasets.api-key}") String apiKey,
      @Value("${financial.datasets.cache.enabled:true}") boolean cacheEnabled,
      ObjectMapper mapper,
      CacheService cacheService) {
    return new FinancialDatasetsService(builder, url, apiKey, cacheEnabled, mapper, cacheService);
  }
}
