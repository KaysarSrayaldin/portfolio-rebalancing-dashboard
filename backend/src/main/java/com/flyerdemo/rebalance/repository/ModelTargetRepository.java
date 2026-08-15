package com.flyerdemo.rebalance.repository;

import com.flyerdemo.rebalance.model.ModelTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelTargetRepository extends JpaRepository<ModelTarget, Long> {
    List<ModelTarget> findByModelPortfolioId(Long modelId);
}
