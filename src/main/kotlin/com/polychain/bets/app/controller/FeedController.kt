package com.polychain.bets.app.controller

import com.polychain.bets.core.dto.FeedResponse
import com.polychain.bets.core.service.DEFAULT_PAGE_SIZE
import com.polychain.bets.core.service.FeedServiceInterface
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feed")
class FeedController(
    private val feedService: FeedServiceInterface
) {
    @GetMapping
    fun getFeed(
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false, defaultValue = "$DEFAULT_PAGE_SIZE") limit: Int,
    ): FeedResponse = runBlocking {
        feedService.getFeed(
            tags = tags,
            encodedCursor = cursor,
            limit = limit,
        )
    }
}