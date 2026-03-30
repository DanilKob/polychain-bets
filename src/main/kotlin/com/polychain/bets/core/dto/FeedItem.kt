package com.polychain.bets.core.dto

import java.time.Instant

data class FeedItem(
    val id: String,
    val name: String,
    val text: String,

    val media: MediaDto,

    val outcomes: List<WagerOutcomeDto>,

    val createdAt: Instant
)
