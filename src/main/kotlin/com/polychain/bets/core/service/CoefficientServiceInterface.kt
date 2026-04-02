package com.polychain.bets.core.service

import java.math.BigDecimal

interface CoefficientServiceInterface {

    /**
     * Calculates the parimutuel coefficient for a single outcome.
     *
     * @param totalPool     Total coins bet across all outcomes (in cents)
     * @param outcomePool   Coins bet on this specific outcome (in cents)
     * @param outcomeCount  Total number of outcomes in the wager
     * @param margin        Platform margin as a fraction in [0, 1), e.g. 0.05 for 5%
     * @return              Coefficient with scale 2, minimum 1.00
     */
    fun calculateCoefficient(
        totalPool: Long,
        outcomePool: Long,
        outcomeCount: Int,
        margin: BigDecimal,
    ): BigDecimal

    /**
     * Calculates coefficients for all outcomes in one pass.
     *
     * @param totalPool     Total coins bet across all outcomes (in cents)
     * @param outcomePools  Map of outcomeId -> coins bet on that outcome (in cents)
     * @param margin        Platform margin as a fraction in [0, 1), e.g. 0.05 for 5%
     * @return              Map of outcomeId -> coefficient
     */
    fun calculateAllCoefficients(
        totalPool: Long,
        outcomePools: Map<String, Long>,
        margin: BigDecimal,
    ): Map<String, BigDecimal>
}