package com.smartpulse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Statistics {

    private String date;
    private Double priceWeightedAverage;
    private Double priceMin;
    private Double priceMax;
    private Integer quantityMin;
    private Integer quantityMax;
    private Integer quantitySum;
}
