package com.polychain.bets.core.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.util.*

@Document(collection = "wager")
data class WagerEntity(
    @Id
    var id: String = UUID.randomUUID().toString(),
    @Field("name")
    var name: String,
    @Field("text")
    var text: String,
    @Field("status")
    var status: WagerStatus,
    @Field("mediaId")
    var mediaId: String,
    @Field("answer_media_Id")
    var answerMediaId: String,

    @Indexed
    @Field("tags")
    var tags: List<String> = emptyList(),

    @CreatedDate
    @Field("created_at")
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Field("updated_at")
    val updatedAt: Instant = Instant.now(),
    @Field("deleted")
    var deleted: Boolean = false
)

enum class WagerStatus {
    INITIALIZING,
    CREATED,
    VERIFICATION_FAILED,
    APPROVED,
    ACTIVE,
    VOTE_CLOSED,
    FINISH_PROCESSING_STARTED,
    FINISH_WINNERS_CALCULATED,
    FINISH_REWARD_DISTRIBUTION_STARTED,
    FINISHED
}