package org.garethjevans.ai.agent.fundamentals;

import static org.assertj.core.api.Assertions.assertThat;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.autoconfigure.McpClientAutoConfiguration;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
      "spring.ai.openai.api-key=${OPENAI_KEY}",
      "spring.ai.openai.chat.options.model=gpt-4o-mini",
      "spring.ai.mcp.client.enabled=true",
      "spring.ai.mcp.client.type=SYNC"
    })
@EnabledIfEnvironmentVariable(named = "OPENAI_KEY", matches = ".+")
@EnableAutoConfiguration(exclude = {McpClientAutoConfiguration.class})
public class AgentTest {

  @Autowired private ChatModel model;

  @Test
  public void canInvokeTool() {
    assertThat(model).isNotNull();

    SyncMcpToolCallbackProvider callbackProvider = getSyncMcpToolCallbackProvider(model);

    ChatClient client =
        ChatClient.builder(model)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .toolCallbacks(callbackProvider.getToolCallbacks())
                    .build())
            .build();

    assertThat(client).isNotNull();

    String response = client.prompt("What tools do you have available?").call().content();

    System.out.println(response);

    assertThat(response).isNotNull();
    assertThat(response).containsIgnoringCase("fundamental");
    response =
        client
            .prompt("What would the fundamentals analysis agent think about AVGO?")
            .call()
            .content();

    System.out.println(response);

    assertThat(response).isNotNull();
    assertThat(response).doesNotContain("error");
  }

  public SyncMcpToolCallbackProvider getSyncMcpToolCallbackProvider(ChatModel chatModel) {
    List<McpSyncClient> clients =
        List.of(getMcpSyncClient("http://localhost:10094/", ChatClient.create(chatModel)));

    return new SyncMcpToolCallbackProvider(clients);
  }

  private McpSyncClient getMcpSyncClient(String baseUrl, ChatClient chatClient) {
    var transport = HttpClientSseClientTransport.builder(baseUrl).build();

    McpSyncClient client =
        McpClient.sync(transport)
            .requestTimeout(Duration.ofSeconds(20))
            .sampling(
                req -> {
                  String message =
                      ((McpSchema.TextContent) req.messages().getFirst().content()).text();
                  String response =
                      chatClient.prompt().system(req.systemPrompt()).user(message).call().content();

                  return McpSchema.CreateMessageResult.builder()
                      .content(new McpSchema.TextContent(response))
                      .build();
                })
            .build();
    client.initialize();

    return client;
  }
}
