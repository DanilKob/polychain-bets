package com.polychain.bets.core.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.util.*

@Document(collection = "wager_outcome_vote")
@CompoundIndex(def = "{'wager_id': 1, 'user_id': 1}", unique = true)
data class WagerOutcomeVoteEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Field("user_id")
    var userId: String,
    @Field("wager_id")
    var wagerId: String,
    @Field("wager_outcome_id")
    var wagerOutcomeId: String,

    @Field("coins_amount")
    val coinCentsAmount: Long,
    @Field("win_coins_amount")
    var winCoinCentsAmount: Long? = null,


    @Field("win_coefficient")
    var winCoefficient: Long? = null,

    @CreatedDate
    @Field("created_at")
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Field("updated_at")
    val updatedAt: Instant = Instant.now(),
    @Field("deleted")
    var deleted: Boolean = false
)
