package com.polychain.bets.core.service

import com.polychain.bets.core.dto.OutcomeStatsDto
import com.polychain.bets.core.dto.WagerStatsDto
import com.polychain.bets.core.entity.WagerOutcomeStats
import com.polychain.bets.core.repository.WagerStatsMongoRepository
import com.polychain.bets.exception.NotFoundException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class WagerStatsService(
    private val wagerStatsMongoRepository: WagerStatsMongoRepository,
    private val coefficientService: CoefficientServiceInterface,
    @Value("\${platform.margin}") private val platformMargin: BigDecimal,
) : WagerStatsServiceInterface {

    override suspend fun getStats(wagerId: String): WagerStatsDto {
        val stats = wagerStatsMongoRepository.findById(wagerId).awaitSingleOrNull()
            ?: throw NotFoundException("Stats not found for wager $wagerId")

        val outcomeCount = stats.outcomes.size
        val coefficients = coefficientService.calculateAllCoefficients(
            totalPool = stats.totalPool,
            outcomePools = stats.outcomes.associate { it.outcomeId to it.pool },
            margin = platformMargin,
        )

        return WagerStatsDto(
            wagerId = stats.wagerId,
            totalPool = stats.totalPool,
            voterCount = stats.voterCount,
            outcomes = stats.outcomes.map { outcome ->
                outcome.toDto(
                    coefficient = coefficients[outcome.outcomeId]
                        ?: coefficientService.calculateCoefficient(
                            totalPool = stats.totalPool,
                            outcomePool = outcome.pool,
                            outcomeCount = outcomeCount,
                            margin = platformMargin,
                        ),
                    totalPool = stats.totalPool,
                )
            },
            updatedAt = stats.updatedAt,
        )
    }

    private fun WagerOutcomeStats.toDto(
        coefficient: BigDecimal,
        totalPool: Long,
    ): OutcomeStatsDto {
        val poolSharePct = if (totalPool > 0L) {
            BigDecimal.valueOf(pool)
                .divide(BigDecimal.valueOf(totalPool), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(1)
        }

        return OutcomeStatsDto(
            outcomeId = outcomeId,
            pool = pool,
            voterCount = voterCount,
            coefficient = coefficient,
            poolSharePct = poolSharePct,
        )
    }
}