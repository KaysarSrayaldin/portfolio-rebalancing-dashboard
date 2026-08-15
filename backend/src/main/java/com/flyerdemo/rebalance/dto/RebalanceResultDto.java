package com.flyerdemo.rebalance.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RebalanceResultDto {
    private BigDecimal totalPortfolioValue;
    private Map<String, BigDecimal> currentAllocation;   // asset class -> weight
    private Map<String, BigDecimal> targetAllocation;    // asset class -> weight
    private Map<String, BigDecimal> projectedAllocation; // asset class -> weight after trades
    private List<ProposedTradeDto> proposedTrades;
    private BigDecimal leftoverCash;
}
