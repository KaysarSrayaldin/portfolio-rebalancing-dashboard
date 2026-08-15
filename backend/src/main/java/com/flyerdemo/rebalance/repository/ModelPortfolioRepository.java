package com.flyerdemo.rebalance.repository;

import com.flyerdemo.rebalance.model.ModelPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelPortfolioRepository extends JpaRepository<ModelPortfolio, Long> {
}
