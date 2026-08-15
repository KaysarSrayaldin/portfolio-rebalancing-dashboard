package com.flyerdemo.rebalance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "security_id", nullable = false)
    private Security security;

    @Enumerated(EnumType.STRING)
    private TradeAction action; // BUY or SELL

    private Integer quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal estimatedPrice;

    @Enumerated(EnumType.STRING)
    private TradeStatus status; // PROPOSED, EXECUTED, CANCELLED

    private LocalDateTime generatedAt;
    private LocalDateTime executedAt;
}
