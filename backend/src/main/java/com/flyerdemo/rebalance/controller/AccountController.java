package com.flyerdemo.rebalance.controller;

import com.flyerdemo.rebalance.model.Account;
import com.flyerdemo.rebalance.model.Holding;
import com.flyerdemo.rebalance.repository.AccountRepository;
import com.flyerdemo.rebalance.repository.HoldingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:3000")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }

    @GetMapping("/client/{clientId}")
    public List<Account> getAccountsForClient(@PathVariable Long clientId) {
        return accountRepository.findByClientId(clientId);
    }

    @GetMapping("/{id}/holdings")
    public List<Holding> getHoldings(@PathVariable Long id) {
        return holdingRepository.findByAccountId(id);
    }
}
