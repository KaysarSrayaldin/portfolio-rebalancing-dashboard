package com.flyerdemo.rebalance.dto;

import com.flyerdemo.rebalance.model.TradeAction;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProposedTradeDto {
    private Long securityId;
    private String ticker;
    private TradeAction action;
    private Integer quantity;
    private BigDecimal estimatedPrice;
    private BigDecimal estimatedValue;
}
