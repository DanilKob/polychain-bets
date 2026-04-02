package com.polychain.bets.finance.repository

import com.polychain.bets.finance.entity.LedgerEntry
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface LedgerEntryRepository : R2dbcRepository<LedgerEntry, UUID> {
    fun findByRefId(refId: String): Flux<LedgerEntry>
}