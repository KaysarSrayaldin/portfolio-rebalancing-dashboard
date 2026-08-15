package com.flyerdemo.rebalance.controller;

import com.flyerdemo.rebalance.dto.RebalanceResultDto;
import com.flyerdemo.rebalance.service.RebalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:3000")
public class RebalanceController {

    @Autowired
    private RebalanceService rebalanceService;

    @PostMapping("/{id}/rebalance")
    public RebalanceResultDto rebalance(@PathVariable Long id) {
        return rebalanceService.rebalance(id);
    }
}
