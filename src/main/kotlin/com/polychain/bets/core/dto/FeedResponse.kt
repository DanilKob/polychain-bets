package com.polychain.bets.core.dto

data class FeedResponse(
    val items: List<FeedItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
    val count: Int
)
