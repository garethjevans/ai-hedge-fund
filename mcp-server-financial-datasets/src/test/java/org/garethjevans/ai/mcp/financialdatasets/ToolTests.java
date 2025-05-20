package org.garethjevans.ai.mcp.financialdatasets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"financial.datasets.api-key=xxx",
		"financial.datasets.url=http://example.com",
})
class ToolTests {

	private WireMockServer wireMockServer;

	private String body = """
                                  {
                                  	"company_facts": {
                                  		"ticker":"AAPL",
                                  		"name":"Apple Inc",
                                  		"cik":"0000320193",
                                  		"industry":"Consumer Electronics",
                                  		"sector":"Technology",
                                  		"category":"Common Stock",
                                  		"exchange":"NASDAQ",
                                  		"is_active":true,
                                  		"listing_date":"1980-12-12",
                                  		"location":"California; U.S.A",
                                  		"market_cap":3148322762540.0,
                                  		"number_of_employees":164000,
                                  		"sec_filings_url":"https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=0000320193",
                                  		"sic_code":"3571.0",
                                  		"sic_industry":"Electronic Computers",
                                  		"sic_sector":"Manufacturing",
                                  		"website_url":"https://www.apple.com",
                                  		"weighted_average_shares":14935826000
                                  	}
                                  }
                                  """;

	@BeforeEach
	public void setup() {
		wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
		wireMockServer.start();
	}

	@Test
	void canParseFacts() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();

		FinancialDatasetsTool.CompanyFactsHolder holder = objectMapper.readValue(body, FinancialDatasetsTool.CompanyFactsHolder.class);
		assertThat(holder).isNotNull();
		assertThat(holder.companyFacts()).isNotNull();
		assertThat(holder.companyFacts().name()).isEqualTo("Apple Inc");
	}

	@Test
	void canQueryAppleTicker() {
		wireMockServer.stubFor(
			get("/company/facts/?ticker=AAPL")
				.withHeader("X-API-KEY", equalTo("xxx"))
				.withHeader("Accept", equalTo("application/json"))
				.willReturn(
					aResponse()
						.withBody(body)
						.withStatus(200)
						.withHeader("Content-Type", "application/json")));


		FinancialDatasetsTool tool = new FinancialDatasetsTool(RestClient.builder(), "http://localhost:" + wireMockServer.port(), "xxx");
		assertThat(tool).isNotNull();

		FinancialDatasetsTool.CompanyFacts facts = tool.companyFacts("AAPL");
		assertThat(facts).isNotNull();
		assertThat(facts.name()).isEqualTo("Apple Inc");
	}
}
