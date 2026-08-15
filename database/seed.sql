-- Run this AFTER starting the Spring Boot app once (so Hibernate creates the tables).
-- Usage: mysql -u root -p rebalance_db < seed.sql

USE rebalance_db;

-- Securities
INSERT INTO securities (ticker, name, asset_class, current_price) VALUES
('VTI', 'Vanguard Total Stock Market ETF', 'EQUITY', 285.50),
('VXUS', 'Vanguard Total International Stock ETF', 'EQUITY', 62.30),
('BND', 'Vanguard Total Bond Market ETF', 'FIXED_INCOME', 72.10),
('BNDX', 'Vanguard Total International Bond ETF', 'FIXED_INCOME', 49.80),
('CASH', 'Cash Sweep', 'CASH', 1.00);

-- Model portfolio: 60/40 growth model
INSERT INTO model_portfolios (name, description) VALUES
('Balanced Growth 60/40', 'Standard moderate-risk model: 60% equity, 35% fixed income, 5% cash buffer');

-- Grab the model id (assumes it's the first row inserted, id = 1 on a fresh DB)
INSERT INTO model_targets (model_id, asset_class, target_weight) VALUES
(1, 'EQUITY', 0.60),
(1, 'FIXED_INCOME', 0.35),
(1, 'CASH', 0.05);

-- Client
INSERT INTO clients (name, risk_profile) VALUES
('Jane Whitfield', 'MODERATE');

-- Account, assigned to the 60/40 model, currently drifted overweight equity
INSERT INTO accounts (client_id, account_type, cash_balance, model_id) VALUES
(1, 'TAXABLE', 5000.00, 1);

-- Holdings: intentionally drifted (equity has run up, overweight vs the 60% target)
INSERT INTO holdings (account_id, security_id, quantity) VALUES
(1, 1, 500),  -- VTI: 500 * 285.50 = 142,750
(1, 2, 200),  -- VXUS: 200 * 62.30  = 12,460
(1, 3, 300),  -- BND:  300 * 72.10  = 21,630
(1, 4, 150);  -- BNDX: 150 * 49.80  = 7,470

-- Rough total value ~ 142750 + 12460 + 21630 + 7470 + 5000 cash = 189,310
-- Current equity weight ~ 82% vs 60% target -> should generate meaningful SELL equity / BUY fixed income trades
