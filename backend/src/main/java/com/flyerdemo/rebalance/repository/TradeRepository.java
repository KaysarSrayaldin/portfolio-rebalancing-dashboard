package com.flyerdemo.rebalance.repository;

import com.flyerdemo.rebalance.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByAccountId(Long accountId);
}
