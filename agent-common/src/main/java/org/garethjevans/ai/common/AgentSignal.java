package org.garethjevans.ai.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentSignal(
    @JsonProperty("agent") String agent,
    @JsonProperty("ticker") String ticker,
    @JsonProperty("signal") Signal signal,
    @JsonProperty("confidence") Float confidence,
    @JsonProperty("reasoning") String reasoning) {

  public AgentSignal withAgent(String agent) {
    return new AgentSignal(agent, ticker, signal, confidence, reasoning);
  }
}
