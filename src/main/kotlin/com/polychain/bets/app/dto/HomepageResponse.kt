package com.polychain.bets.app.dto

import com.polychain.bets.core.dto.FeedResponse

data class HomepageResponse(
    val user: UserResponse,
    val feed: FeedResponse,
)
