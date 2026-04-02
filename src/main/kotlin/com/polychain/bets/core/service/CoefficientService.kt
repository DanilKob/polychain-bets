package com.polychain.bets.core.service

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CoefficientService : CoefficientServiceInterface {

    override fun calculateCoefficient(
        totalPool: Long,
        outcomePool: Long,
        outcomeCount: Int,
        margin: BigDecimal,
    ): BigDecimal {
        require(margin >= BigDecimal.ZERO && margin < BigDecimal.ONE) {
            "Margin must be in [0, 1), got $margin"
        }
        require(outcomeCount > 0) {
            "outcomeCount must be positive, got $outcomeCount"
        }

        val netMultiplier = BigDecimal.ONE.subtract(margin)

        // No bets placed on this outcome yet (or wager has no pool at all):
        // assume equal probability across all outcomes and apply margin.
        // Formula: outcomeCount × (1 − margin)
        if (totalPool <= 0L || outcomePool <= 0L) {
            return BigDecimal.valueOf(outcomeCount.toLong())
                .multiply(netMultiplier)
                .setScale(2, RoundingMode.HALF_UP)
        }

        // All money is on this outcome — no losers to pay winners.
        // Net return after margin is (1 − margin), which may be < 1.0.
        // Clamp to 1.00 so bettors always get at least their stake back.
        val losingPool = totalPool - outcomePool
        if (losingPool <= 0L) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP)
        }

        // Standard parimutuel with platform margin:
        // coefficient = (totalPool × (1 − margin)) / outcomePool
        val coefficient = BigDecimal.valueOf(totalPool)
            .multiply(netMultiplier)
            .divide(BigDecimal.valueOf(outcomePool), 10, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP)

        // Safety floor — coefficient must never be below 1.00
        return coefficient.max(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP))
    }

    override fun calculateAllCoefficients(
        totalPool: Long,
        outcomePools: Map<String, Long>,
        margin: BigDecimal,
    ): Map<String, BigDecimal> {
        val outcomeCount = outcomePools.size
        return outcomePools.mapValues { (_, pool) ->
            calculateCoefficient(
                totalPool = totalPool,
                outcomePool = pool,
                outcomeCount = outcomeCount,
                margin = margin,
            )
        }
    }
}