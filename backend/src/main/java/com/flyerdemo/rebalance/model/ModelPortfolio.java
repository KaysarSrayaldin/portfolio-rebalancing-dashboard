package com.flyerdemo.rebalance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "model_portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @OneToMany(mappedBy = "modelPortfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelTarget> targets;
}
