package com.pms.analytics.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskEventDto {
    private UUID portfolioId;
    private float avgRateOfReturn;
    private float sharpeRatio;
    private float sortinoRatio;
}
