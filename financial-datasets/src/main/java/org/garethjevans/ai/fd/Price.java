package org.garethjevans.ai.fd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Price(
    @JsonProperty("open") BigDecimal open,
    @JsonProperty("close") BigDecimal close,
    @JsonProperty("high") BigDecimal high,
    @JsonProperty("low") BigDecimal low,
    @JsonProperty("volume") BigDecimal volume,
    @JsonProperty("time") String time,
    @JsonProperty("time_milliseconds") BigInteger timeMillisecond) {}
