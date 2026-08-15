package com.flyerdemo.rebalance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "securities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Security {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticker;

    private String name;

    @Enumerated(EnumType.STRING)
    private AssetClass assetClass; // EQUITY, FIXED_INCOME, CASH

    @Column(precision = 19, scale = 4)
    private BigDecimal currentPrice;
}
