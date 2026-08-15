package com.flyerdemo.rebalance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "model_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    @JsonIgnore
    private ModelPortfolio modelPortfolio;

    @Enumerated(EnumType.STRING)
    private AssetClass assetClass;

    // Stored as a fraction, e.g. 0.60 for 60%
    @Column(precision = 5, scale = 4)
    private BigDecimal targetWeight;
}
