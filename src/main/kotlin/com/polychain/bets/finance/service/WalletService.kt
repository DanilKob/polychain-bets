package com.polychain.bets.finance.service

import com.polychain.bets.finance.entity.CurrencyType
import com.polychain.bets.finance.entity.LedgerEntry
import com.polychain.bets.finance.entity.LedgerEntryType
import com.polychain.bets.finance.entity.WalletEntity
import com.polychain.bets.finance.entity.WalletType
import com.polychain.bets.finance.repository.LedgerEntryRepository
import com.polychain.bets.finance.repository.WalletRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger {}

const val PLATFORM_USER_ID = "platform"

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val ledgerRepository: LedgerEntryRepository,
    private val databaseClient: DatabaseClient,
) : WalletServiceInterface {

    override suspend fun getWallet(userId: String, currency: CurrencyType): WalletEntity {
        return walletRepository.findByUserIdAndCurrency(userId, currency).awaitSingleOrNull()
            ?: walletRepository.save(
                WalletEntity(userId = userId, walletType = WalletType.USER, currency = currency)
            ).awaitSingle()
    }

    override suspend fun getBalance(userId: String, currency: CurrencyType): Long {
        return getWallet(userId, currency).available
    }

    @Transactional
    override suspend fun transfer(
        fromUserId: String,
        toUserId: String,
        amount: Long,
        currency: CurrencyType,
        type: LedgerEntryType,
        refId: String,
    ) {
        require(amount > 0) { "Transfer amount must be positive, got $amount" }

        // SELECT FOR UPDATE — serializes concurrent transfers from the same wallet
        val fromWallet = selectForUpdate(fromUserId, currency)
            ?: throw InsufficientFundsException("Wallet not found for user $fromUserId")

        if (fromWallet.available < amount) {
            throw InsufficientFundsException(
                "Insufficient funds for user=$fromUserId: available=${fromWallet.available}, required=$amount"
            )
        }

        val toWallet = getWallet(toUserId, currency)

        databaseClient.sql("UPDATE wallets SET available = available - :amount, updated_at = :now WHERE id = :id")
            .bind("amount", amount)
            .bind("now", Instant.now())
            .bind("id", fromWallet.id)
            .fetch().rowsUpdated().awaitSingle()

        databaseClient.sql("UPDATE wallets SET available = available + :amount, updated_at = :now WHERE id = :id")
            .bind("amount", amount)
            .bind("now", Instant.now())
            .bind("id", toWallet.id)
            .fetch().rowsUpdated().awaitSingle()

        ledgerRepository.saveAll(
            listOf(
                LedgerEntry(walletId = fromWallet.id, type = type, amount = amount, refId = refId),
                LedgerEntry(walletId = toWallet.id,   type = type, amount = amount, refId = refId),
            )
        ).collectList().awaitSingle()

        logger.info { "Transfer complete: $fromUserId → $toUserId, amount=$amount, type=$type, ref=$refId" }
    }

    @Transactional
    override suspend fun deposit(
        userId: String,
        amount: Long,
        currency: CurrencyType,
        externalRef: String,
    ) {
        require(amount > 0) { "Deposit amount must be positive, got $amount" }

        val wallet = getWallet(userId, currency)

        databaseClient.sql("UPDATE wallets SET available = available + :amount, updated_at = :now WHERE id = :id")
            .bind("amount", amount)
            .bind("now", Instant.now())
            .bind("id", wallet.id)
            .fetch().rowsUpdated().awaitSingle()

        ledgerRepository.save(
            LedgerEntry(walletId = wallet.id, type = LedgerEntryType.DEPOSIT, amount = amount, refId = externalRef)
        ).awaitSingle()

        logger.info { "Deposit: userId=$userId, amount=$amount, ref=$externalRef" }
    }

    private suspend fun selectForUpdate(userId: String, currency: CurrencyType): WalletEntity? {
        return databaseClient.sql(
            "SELECT * FROM wallets WHERE user_id = :userId AND currency = :currency::currency_type FOR UPDATE"
        )
            .bind("userId", userId)
            .bind("currency", currency.name)
            .map { row, _ ->
                WalletEntity(
                    id = row.get("id", java.util.UUID::class.java)
                        ?: error("wallet.id is null for userId=$userId"),
                    userId = row.get("user_id", String::class.java)
                        ?: error("wallet.user_id is null for userId=$userId"),
                    walletType = WalletType.valueOf(
                        row.get("wallet_type", String::class.java)
                            ?: error("wallet.wallet_type is null for userId=$userId")
                    ),
                    currency = CurrencyType.valueOf(
                        row.get("currency", String::class.java)
                            ?: error("wallet.currency is null for userId=$userId")
                    ),
                    available = row.get("available", Long::class.java)
                        ?: error("wallet.available is null for userId=$userId"),
                    createdAt = row.get("created_at", Instant::class.java)
                        ?: error("wallet.created_at is null for userId=$userId"),
                    updatedAt = row.get("updated_at", Instant::class.java)
                        ?: error("wallet.updated_at is null for userId=$userId"),
                )
            }
            .one()
            .awaitSingleOrNull()
    }
}