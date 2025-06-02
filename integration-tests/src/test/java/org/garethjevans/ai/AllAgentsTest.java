package org.garethjevans.ai;

import org.garethjevans.ai.agent.michaelburry.AgentMichaelBurryApplication;
import org.garethjevans.ai.agent.warrenbuffett.AgentWarrenBuffettApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {AgentWarrenBuffettApplication.class, AgentMichaelBurryApplication.class})
public class AllAgentsTest {

  @Test
  public void testAllAgents() {
    //    McpClientTransport transport =
    //            HttpClientSseClientTransport.builder("http://localhost:" + port).build();
    //    McpSyncClient mcpClient =
    //            McpClient.sync(transport).requestTimeout(Duration.ofSeconds(60)).build();
    //
    //    mcpClient.initialize();
  }
}
