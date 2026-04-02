package com.polychain.bets.core.service

import com.polychain.bets.core.dto.WagerStatsDto

interface WagerStatsServiceInterface {
    suspend fun getStats(wagerId: String): WagerStatsDto
}