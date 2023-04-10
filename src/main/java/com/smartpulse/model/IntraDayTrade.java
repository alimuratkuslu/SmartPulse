package com.smartpulse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class IntraDayTrade {

    private Integer id;
    private String date;
    private String conract;
    private Double price;
    private Integer quantity;
}
