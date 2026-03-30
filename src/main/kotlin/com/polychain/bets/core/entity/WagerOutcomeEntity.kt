package com.polychain.bets.core.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.util.*

@Document(collection = "wager_outcome")
@CompoundIndex(def = "{'wagerId': 1}", unique = false)
data class WagerOutcomeEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Field("wager_id")
    var wagerId: String,
    @Field("description")
    var description: String,
    @Field("win")
    var win: Boolean = false,

    @CreatedDate
    @Field("created_at")
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Field("updated_at")
    val updatedAt: Instant = Instant.now(),
    @Field("deleted")
    var deleted: Boolean = false
)
