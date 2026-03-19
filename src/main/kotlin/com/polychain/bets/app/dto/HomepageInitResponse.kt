package com.polychain.bets.app.dto

data class HomepageInitResponse(
    val user: UserForInitResponse,
    val feed: List<FeedDto>,
)
