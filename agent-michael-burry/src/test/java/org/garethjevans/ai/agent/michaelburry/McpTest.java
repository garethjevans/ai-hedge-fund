package org.garethjevans.ai.agent.michaelburry;

import static org.assertj.core.api.Assertions.assertThat;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.openai.autoconfigure.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"server.shutdown=immediate"})
@EnableAutoConfiguration(exclude={OpenAiChatAutoConfiguration.class,
        OpenAiEmbeddingAutoConfiguration.class,
OpenAiAudioSpeechAutoConfiguration.class,
OpenAiImageAutoConfiguration.class,
OpenAiAudioTranscriptionAutoConfiguration.class,
OpenAiModerationAutoConfiguration.class})
class McpTest {

  @LocalServerPort private int port;

  @Test
  void canQueryAppleTicker() {
    McpClientTransport transport =
        HttpClientSseClientTransport.builder("http://localhost:" + port).build();
    McpSyncClient mcpClient =
        McpClient.sync(transport).requestTimeout(Duration.ofSeconds(60)).build();

    mcpClient.initialize();

    mcpClient.setLoggingLevel(McpSchema.LoggingLevel.DEBUG);

    McpSchema.ListToolsResult tools = mcpClient.listTools();
    assertThat(tools).isNotNull();
    assertThat(tools.tools()).isNotNull();
    assertThat(tools.tools()).hasSizeGreaterThan(0);

    tools.tools().forEach(System.out::println);

    McpSchema.CallToolResult result =
        mcpClient.callTool(
            new McpSchema.CallToolRequest("michael_burry_analysis", "{\"ticker\":\"AVGO\"}"));

    result.content().forEach(System.out::println);
  }
}
