package com.polychain.bets.core.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "wager_stats")
data class WagerStatsEntity(
    @Id
    val wagerId: String,

    @Field("total_pool")
    val totalPool: Long = 0L,           // sum of all coinsAmount across all outcomes

    @Field("voter_count")
    val voterCount: Int = 0,            // total number of votes across all outcomes

    @Field("outcomes")
    val outcomes: List<WagerOutcomeStats> = emptyList(),

    @LastModifiedDate
    @Field("updated_at")
    val updatedAt: Instant = Instant.now(),
)

data class WagerOutcomeStats(
    @Field("outcome_id")
    val outcomeId: String,

    @Field("pool")
    val pool: Long = 0L,                // sum of coinsAmount for this outcome

    @Field("voter_count")
    val voterCount: Int = 0,            // number of votes for this outcome

    // Implied coefficient = totalPool / pool (parimutuel)
    // Stored as basis points (e.g. 185 = 1.85x) to avoid floating point in Mongo
    @Field("coefficient_bp")
    val coefficientBp: Long = 0L,
) {
    // Convenience for application code
    val coefficient: Double get() = coefficientBp / 100.0
}