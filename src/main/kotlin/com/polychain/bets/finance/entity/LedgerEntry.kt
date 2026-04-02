package com.polychain.bets.finance.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("ledger_entries")
data class LedgerEntry(
    @Id val id: UUID = UUID.randomUUID(),
    @Column("wallet_id")  val walletId: UUID,
    @Column("type")       val type: LedgerEntryType,
    @Column("amount")     val amount: Long,
    @Column("ref_id")     val refId: String? = null,
    @Column("created_at") val createdAt: Instant = Instant.now(),
)