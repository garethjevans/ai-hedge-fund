package org.garethjevans.ai.agent.michaelburry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AgentTest {

  @Autowired private SyncMcpToolCallbackProvider callbackProvider;

  @Test
  public void canInvokeTool() {
    assertThat(callbackProvider).isNotNull();

    OpenAiApi api = OpenAiApi.builder().apiKey(System.getenv("OPENAI_KEY")).build();
    ChatModel model = OpenAiChatModel.builder().openAiApi(api).build();

    ChatClient client = ChatClient.builder(model).build();
    assertThat(client).isNotNull();

    String response =
        client
            .prompt(
                new Prompt(
                    "What AI Stock agents to you have available?",
                    OpenAiChatOptions.builder()
                        .toolCallbacks(callbackProvider.getToolCallbacks())
                        .build()))
            .call()
            .content();

    System.out.println(response);
    
    assertThat(response).isNotNull();
  }
}
