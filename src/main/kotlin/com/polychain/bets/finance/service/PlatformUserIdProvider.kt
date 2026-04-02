package com.polychain.bets.finance.service

import org.springframework.stereotype.Service

@Service
class PlatformUserIdProvider: PlatformUserIdProviderInterface {
    override fun getPlatformUserId(refId: String): String {
        val shardId = refId.hashCode() % 10
        return "platform_$shardId"
    }
}