package com.polychain.bets.finance.service

interface PlatformUserIdProviderInterface {
    fun getPlatformUserId(refId: String): String
}