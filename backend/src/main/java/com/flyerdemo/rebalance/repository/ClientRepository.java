package com.flyerdemo.rebalance.repository;

import com.flyerdemo.rebalance.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
