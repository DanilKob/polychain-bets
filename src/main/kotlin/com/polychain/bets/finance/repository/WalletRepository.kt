package com.polychain.bets.finance.repository

import com.polychain.bets.finance.entity.CurrencyType
import com.polychain.bets.finance.entity.WalletEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface WalletRepository : R2dbcRepository<WalletEntity, UUID> {
    fun findByUserIdAndCurrency(userId: String, currency: CurrencyType): Mono<WalletEntity>
}