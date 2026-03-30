package com.polychain.bets.core.service

import com.polychain.bets.app.dto.CreateWagerRequest
import com.polychain.bets.core.dto.CursorParams
import com.polychain.bets.core.dto.WagerDto
import com.polychain.bets.core.dto.WagerItem
import com.polychain.bets.core.entity.WagerMediaEntity
import com.polychain.bets.core.entity.WagerOutcomeEntity

interface WagerServiceInterface {
    suspend fun searchForFeed(
        tags: List<String>? = null,
        cursor: CursorParams? = null,
        limit: Int = 10,
    ): List<WagerDto>

    suspend fun searchItemsForFeed(tags: List<String>?, cursor: CursorParams?, limit: Int): List<WagerItem>
    suspend fun getOutcomesByWagerIds(wagerIds: List<String>): Map<String, List<WagerOutcomeEntity>>
    suspend fun getWagerMediaByWagerIds(mediaIds: List<String>): Map<String, WagerMediaEntity?>
    suspend fun createDebugWagerForController(request: CreateWagerRequest)
    suspend fun createWagersForController(n: Int)
}