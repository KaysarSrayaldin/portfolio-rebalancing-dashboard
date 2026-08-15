package com.flyerdemo.rebalance.repository;

import com.flyerdemo.rebalance.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByAccountId(Long accountId);
}
