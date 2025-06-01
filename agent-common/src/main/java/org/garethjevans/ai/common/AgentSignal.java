package org.garethjevans.ai.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentSignal(
    @JsonProperty("signal") Signal signal,
    @JsonProperty("confidence") Float confidence,
    @JsonProperty("reasoning") String reasoning) {}
