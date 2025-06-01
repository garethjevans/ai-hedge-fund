package org.garethjevans.ai.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Result(
    @JsonProperty("score") int score,
    @JsonProperty("max_score") int maxScore,
    @JsonProperty("details") String details) {}
