package com.polychain.bets.app.service

import com.polychain.bets.app.dto.HomepageInitResponse

interface HomepageServiceInterface {
    suspend fun init(userId: String) : HomepageInitResponse
}