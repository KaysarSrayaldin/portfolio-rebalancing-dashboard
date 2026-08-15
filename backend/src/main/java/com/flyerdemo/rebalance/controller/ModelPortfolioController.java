package com.flyerdemo.rebalance.controller;

import com.flyerdemo.rebalance.model.ModelPortfolio;
import com.flyerdemo.rebalance.repository.ModelPortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@CrossOrigin(origins = "http://localhost:3000")
public class ModelPortfolioController {

    @Autowired
    private ModelPortfolioRepository modelPortfolioRepository;

    @GetMapping
    public List<ModelPortfolio> getAllModels() {
        return modelPortfolioRepository.findAll();
    }

    @PostMapping
    public ModelPortfolio createModel(@RequestBody ModelPortfolio model) {
        return modelPortfolioRepository.save(model);
    }

    @GetMapping("/{id}")
    public ModelPortfolio getModel(@PathVariable Long id) {
        return modelPortfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Model not found: " + id));
    }
}
