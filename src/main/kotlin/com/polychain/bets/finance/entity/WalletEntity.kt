package com.polychain.bets.finance.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("wallets")
data class WalletEntity(
    @Id val id: UUID = UUID.randomUUID(),
    @Column("user_id")     val userId: String,
    @Column("wallet_type") val walletType: WalletType,
    @Column("currency")    val currency: CurrencyType,
    @Column("available")   val available: Long = 0L,
    @Column("created_at")  val createdAt: Instant = Instant.now(),
    @Column("updated_at")  val updatedAt: Instant = Instant.now(),
)