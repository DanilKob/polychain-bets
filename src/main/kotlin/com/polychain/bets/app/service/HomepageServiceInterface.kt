package com.polychain.bets.app.service

import com.polychain.bets.app.dto.HomepageResponse

interface HomepageServiceInterface {
    suspend fun getHomepage(userId: String) : HomepageResponse
}