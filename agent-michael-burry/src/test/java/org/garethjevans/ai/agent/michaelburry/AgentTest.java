package org.garethjevans.ai.agent.michaelburry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
      "spring.ai.openai.api-key=${OPENAI_KEY}",
      "spring.ai.openai.chat.model=gpt-4o-mini",
      "spring.ai.mcp.client.enabled=true",
      // "spring.ai.mcp.client.toolcallback.enabled=true",
      "spring.ai.mcp.client.type=sync",
      "spring.ai.mcp.client.sse.connections.michaelburry.url=http://localhost:10091/",
      // "spring.ai.mcp.client.initialized=false",
    })
public class AgentTest {

  @Autowired private ChatModel model;

  @Test
  public void canInvokeTool() {
    assertThat(model).isNotNull();

    ChatClient client = ChatClient.builder(model).build();
    assertThat(client).isNotNull();

    String response = client.prompt("What tools do you have available?").call().content();

    System.out.println(response);

    assertThat(response).isNotNull();
  }
}
