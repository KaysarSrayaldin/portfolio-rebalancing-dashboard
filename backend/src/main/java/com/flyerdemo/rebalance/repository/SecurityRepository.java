package com.flyerdemo.rebalance.repository;

import com.flyerdemo.rebalance.model.Security;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityRepository extends JpaRepository<Security, Long> {
}
