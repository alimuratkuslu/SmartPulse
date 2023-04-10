package com.smartpulse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonPropertyOrder({ "intraDayTradeHistoryList", "statistics"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Body {
    @JsonProperty
    private List<IntraDayTrade> intraDayTradeHistoryList;
    @JsonProperty
    private List<Statistics> statistics;
}
