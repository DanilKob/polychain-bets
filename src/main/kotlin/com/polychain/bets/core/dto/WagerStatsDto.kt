package com.polychain.bets.core.dto

import java.math.BigDecimal
import java.time.Instant

data class WagerStatsDto(
    val wagerId: String,
    val totalPool: Long,
    val voterCount: Int,
    val outcomes: List<OutcomeStatsDto>,
    val updatedAt: Instant,
)

data class OutcomeStatsDto(
    val outcomeId: String,
    val pool: Long,
    val voterCount: Int,
    val coefficient: BigDecimal,
    // Share of the total pool as a percentage [0, 100], useful for UI progress bars
    val poolSharePct: BigDecimal,
)