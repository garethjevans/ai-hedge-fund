package org.garethjevans.ai.fd;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {})
class McpTests {

  @LocalServerPort private int port;

  private WireMockServer wireMockServer;

  //  private String body =
  //      """
  //                                  {
  //                                  	"company_facts": {
  //                                  		"ticker":"AAPL",
  //                                  		"name":"Apple Inc",
  //                                  		"cik":"0000320193",
  //                                  		"industry":"Consumer Electronics",
  //                                  		"sector":"Technology",
  //                                  		"category":"Common Stock",
  //                                  		"exchange":"NASDAQ",
  //                                  		"is_active":true,
  //                                  		"listing_date":"1980-12-12",
  //                                  		"location":"California; U.S.A",
  //                                  		"market_cap":3148322762540.0,
  //                                  		"number_of_employees":164000,
  //
  //	"sec_filings_url":"https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=0000320193",
  //                                  		"sic_code":"3571.0",
  //                                  		"sic_industry":"Electronic Computers",
  //                                  		"sic_sector":"Manufacturing",
  //                                  		"website_url":"https://www.apple.com",
  //                                  		"weighted_average_shares":14935826000
  //                                  	}
  //                                  }
  //                                  """;
  //
  //  @BeforeEach
  //  public void setup() {
  //    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
  //    wireMockServer.start();
  //  }
  //
  //  @Test
  //  void canQueryAppleTicker() {
  //    wireMockServer.stubFor(
  //        WireMock.get("/company/facts/?ticker=AAPL")
  //            .withHeader("X-API-KEY", WireMock.equalTo("xxx"))
  //            .withHeader("Accept", WireMock.equalTo("application/json"))
  //            .willReturn(
  //                WireMock.aResponse()
  //                    .withBody(body)
  //                    .withStatus(200)
  //                    .withHeader("Content-Type", "application/json")));
  //
  //    McpClientTransport transport =
  //        HttpClientSseClientTransport.builder("http://localhost:" + port).build();
  //    McpSyncClient mcpClient =
  //        McpClient.sync(transport).requestTimeout(Duration.ofSeconds(5)).build();
  //
  //    mcpClient.initialize();
  //
  //    mcpClient.setLoggingLevel(McpSchema.LoggingLevel.DEBUG);
  //
  //    McpSchema.ListToolsResult tools = mcpClient.listTools();
  //    assertThat(tools).isNotNull();
  //    assertThat(tools.tools()).isNotNull();
  //    assertThat(tools.tools()).hasSizeGreaterThan(1);
  //
  //    McpSchema.CallToolResult result =
  //        mcpClient.callTool(new McpSchema.CallToolRequest("companyFacts",
  // "{\"ticker\":\"AAPL\"}"));
  //    result.content().forEach(System.out::println);
  //
  //    transport.close();
  //    mcpClient.closeGracefully();
  //  }
}
