package com.flyerdemo.rebalance.service;

import com.flyerdemo.rebalance.dto.ProposedTradeDto;
import com.flyerdemo.rebalance.dto.RebalanceResultDto;
import com.flyerdemo.rebalance.model.*;
import com.flyerdemo.rebalance.repository.AccountRepository;
import com.flyerdemo.rebalance.repository.HoldingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Core rebalancing engine.
 *
 * Algorithm overview:
 *  1. Compute current dollar value held in each asset class (equity / fixed income / cash).
 *  2. Compare against the account's target model weights to find dollar "drift" per class.
 *  3. For overweight classes: sell proportionally across currently held securities in that
 *     class, rounding down to whole shares, skipping trades below a minimum dollar threshold.
 *  4. For underweight classes: buy proportionally across currently held securities in that
 *     class using available cash (existing cash + cash freed up by sells), respecting a
 *     minimum cash buffer so the account never goes to zero cash.
 *
 * Note: this v1 implementation only rebalances among securities the account already holds
 * within a given asset class. It does not invent brand-new security purchases for an empty
 * asset class bucket - that's a natural v2 extension (e.g. pick a default ETF per asset class).
 */
@Service
public class RebalanceService {

    private static final BigDecimal MIN_TRADE_DOLLAR_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal MIN_CASH_BUFFER_PCT = new BigDecimal("0.02"); // keep >=2% cash

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;

    @Autowired
    public RebalanceService(AccountRepository accountRepository, HoldingRepository holdingRepository) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
    }

    public RebalanceResultDto rebalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + accountId));

        if (account.getTargetModel() == null) {
            throw new IllegalStateException("Account has no target model assigned");
        }

        List<Holding> holdings = holdingRepository.findByAccountId(accountId);

        // Step 1: current value per asset class
        Map<AssetClass, BigDecimal> currentValueByClass = new EnumMap<>(AssetClass.class);
        for (AssetClass ac : AssetClass.values()) currentValueByClass.put(ac, BigDecimal.ZERO);

        Map<AssetClass, List<Holding>> holdingsByClass = new EnumMap<>(AssetClass.class);
        for (AssetClass ac : AssetClass.values()) holdingsByClass.put(ac, new ArrayList<>());

        for (Holding h : holdings) {
            AssetClass ac = h.getSecurity().getAssetClass();
            BigDecimal value = h.getSecurity().getCurrentPrice()
                    .multiply(BigDecimal.valueOf(h.getQuantity()));
            currentValueByClass.merge(ac, value, BigDecimal::add);
            holdingsByClass.get(ac).add(h);
        }

        BigDecimal cashBalance = account.getCashBalance() == null ? BigDecimal.ZERO : account.getCashBalance();
        currentValueByClass.merge(AssetClass.CASH, cashBalance, BigDecimal::add);

        BigDecimal totalValue = currentValueByClass.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Account has zero or negative total value; cannot rebalance");
        }

        // Step 2: target value per class from the model
        Map<AssetClass, BigDecimal> targetWeightByClass = new EnumMap<>(AssetClass.class);
        for (AssetClass ac : AssetClass.values()) targetWeightByClass.put(ac, BigDecimal.ZERO);
        for (ModelTarget t : account.getTargetModel().getTargets()) {
            targetWeightByClass.put(t.getAssetClass(), t.getTargetWeight());
        }

        Map<AssetClass, BigDecimal> targetValueByClass = new EnumMap<>(AssetClass.class);
        for (AssetClass ac : AssetClass.values()) {
            targetValueByClass.put(ac, totalValue.multiply(targetWeightByClass.get(ac)));
        }

        // Step 3: drift = target - current. Negative => sell, positive => buy.
        Map<AssetClass, BigDecimal> driftByClass = new EnumMap<>(AssetClass.class);
        for (AssetClass ac : AssetClass.values()) {
            driftByClass.put(ac, targetValueByClass.get(ac).subtract(currentValueByClass.get(ac)));
        }

        List<ProposedTradeDto> trades = new ArrayList<>();
        BigDecimal cashRaisedFromSells = BigDecimal.ZERO;

        // Step 4: generate SELL trades for overweight (non-cash) asset classes
        for (AssetClass ac : List.of(AssetClass.EQUITY, AssetClass.FIXED_INCOME)) {
            BigDecimal drift = driftByClass.get(ac);
            if (drift.negate().compareTo(MIN_TRADE_DOLLAR_THRESHOLD) > 0) {
                BigDecimal dollarsToSell = drift.negate();
                BigDecimal classValue = currentValueByClass.get(ac);
                if (classValue.compareTo(BigDecimal.ZERO) <= 0) continue;

                for (Holding h : holdingsByClass.get(ac)) {
                    BigDecimal holdingValue = h.getSecurity().getCurrentPrice()
                            .multiply(BigDecimal.valueOf(h.getQuantity()));
                    // proportional share of the sell within this asset class
                    BigDecimal proportion = holdingValue.divide(classValue, 10, RoundingMode.HALF_UP);
                    BigDecimal dollarsForThisHolding = dollarsToSell.multiply(proportion);

                    int sharesToSell = dollarsForThisHolding
                            .divide(h.getSecurity().getCurrentPrice(), 0, RoundingMode.DOWN)
                            .intValue();
                    sharesToSell = Math.min(sharesToSell, h.getQuantity());

                    if (sharesToSell > 0) {
                        BigDecimal estValue = h.getSecurity().getCurrentPrice().multiply(BigDecimal.valueOf(sharesToSell));
                        trades.add(new ProposedTradeDto(
                                h.getSecurity().getId(), h.getSecurity().getTicker(),
                                TradeAction.SELL, sharesToSell, h.getSecurity().getCurrentPrice(), estValue));
                        cashRaisedFromSells = cashRaisedFromSells.add(estValue);
                    }
                }
            }
        }

        // Step 5: figure out cash available for buys, respecting the minimum cash buffer
        BigDecimal minCashBuffer = totalValue.multiply(MIN_CASH_BUFFER_PCT);
        BigDecimal availableCash = cashBalance.add(cashRaisedFromSells).subtract(minCashBuffer);
        if (availableCash.compareTo(BigDecimal.ZERO) < 0) availableCash = BigDecimal.ZERO;

        // Step 6: generate BUY trades for underweight (non-cash) asset classes
        for (AssetClass ac : List.of(AssetClass.EQUITY, AssetClass.FIXED_INCOME)) {
            BigDecimal drift = driftByClass.get(ac);
            if (drift.compareTo(MIN_TRADE_DOLLAR_THRESHOLD) > 0 && !holdingsByClass.get(ac).isEmpty()) {
                BigDecimal dollarsToBuy = drift.min(availableCash);
                if (dollarsToBuy.compareTo(MIN_TRADE_DOLLAR_THRESHOLD) < 0) continue;

                BigDecimal classValue = currentValueByClass.get(ac);
                // avoid divide-by-zero if the class is currently empty but held elsewhere
                BigDecimal baseForProportion = classValue.compareTo(BigDecimal.ZERO) > 0
                        ? classValue
                        : BigDecimal.ONE;

                BigDecimal dollarsSpent = BigDecimal.ZERO;
                for (Holding h : holdingsByClass.get(ac)) {
                    BigDecimal holdingValue = h.getSecurity().getCurrentPrice()
                            .multiply(BigDecimal.valueOf(h.getQuantity()));
                    BigDecimal proportion = classValue.compareTo(BigDecimal.ZERO) > 0
                            ? holdingValue.divide(baseForProportion, 10, RoundingMode.HALF_UP)
                            : BigDecimal.ONE.divide(BigDecimal.valueOf(holdingsByClass.get(ac).size()), 10, RoundingMode.HALF_UP);
                    BigDecimal dollarsForThisHolding = dollarsToBuy.multiply(proportion);

                    int sharesToBuy = dollarsForThisHolding
                            .divide(h.getSecurity().getCurrentPrice(), 0, RoundingMode.DOWN)
                            .intValue();

                    if (sharesToBuy > 0) {
                        BigDecimal estValue = h.getSecurity().getCurrentPrice().multiply(BigDecimal.valueOf(sharesToBuy));
                        trades.add(new ProposedTradeDto(
                                h.getSecurity().getId(), h.getSecurity().getTicker(),
                                TradeAction.BUY, sharesToBuy, h.getSecurity().getCurrentPrice(), estValue));
                        dollarsSpent = dollarsSpent.add(estValue);
                    }
                }
                availableCash = availableCash.subtract(dollarsSpent);
            }
        }

        // Step 7: build projected allocation after trades (for the before/after UI comparison)
        Map<AssetClass, BigDecimal> projectedValueByClass = new EnumMap<>(currentValueByClass);
        for (ProposedTradeDto t : trades) {
            AssetClass ac = holdingsByClass.values().stream()
                    .flatMap(List::stream)
                    .filter(h -> h.getSecurity().getId().equals(t.getSecurityId()))
                    .findFirst()
                    .map(h -> h.getSecurity().getAssetClass())
                    .orElse(AssetClass.EQUITY);
            BigDecimal signedValue = t.getAction() == TradeAction.BUY ? t.getEstimatedValue() : t.getEstimatedValue().negate();
            projectedValueByClass.merge(ac, signedValue, BigDecimal::add);
        }
        BigDecimal netCashMovement = cashRaisedFromSells.subtract(
                trades.stream().filter(t -> t.getAction() == TradeAction.BUY)
                        .map(ProposedTradeDto::getEstimatedValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        projectedValueByClass.merge(AssetClass.CASH, netCashMovement, BigDecimal::add);

        return new RebalanceResultDto(
                totalValue,
                toWeightMap(currentValueByClass, totalValue),
                toWeightMap(targetValueByClass, totalValue),
                toWeightMap(projectedValueByClass, totalValue),
                trades,
                cashBalance.add(netCashMovement)
        );
    }

    private Map<String, BigDecimal> toWeightMap(Map<AssetClass, BigDecimal> valueByClass, BigDecimal totalValue) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (AssetClass ac : AssetClass.values()) {
            BigDecimal weight = valueByClass.getOrDefault(ac, BigDecimal.ZERO)
                    .divide(totalValue, 4, RoundingMode.HALF_UP);
            result.put(ac.name(), weight);
        }
        return result;
    }
}
