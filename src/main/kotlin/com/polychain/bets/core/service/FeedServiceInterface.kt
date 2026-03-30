package com.polychain.bets.core.service

import com.polychain.bets.core.dto.FeedResponse

const val DEFAULT_PAGE_SIZE = 5
const val MAX_PAGE_SIZE = 10

interface FeedServiceInterface {
    suspend fun getFeed(tags: List<String>?, encodedCursor: String?, limit: Int = DEFAULT_PAGE_SIZE): FeedResponse
}