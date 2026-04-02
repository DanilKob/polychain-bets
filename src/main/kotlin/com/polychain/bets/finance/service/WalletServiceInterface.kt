package com.polychain.bets.finance.service

import com.polychain.bets.finance.entity.CurrencyType
import com.polychain.bets.finance.entity.LedgerEntryType
import com.polychain.bets.finance.entity.WalletEntity

interface WalletServiceInterface {

    suspend fun getWallet(userId: String, currency: CurrencyType): WalletEntity

    suspend fun getBalance(userId: String, currency: CurrencyType): Long

    /**
     * Atomically debits [fromUserId] and credits [toUserId] by [amount].
     * Throws [InsufficientFundsException] if [fromUserId] available balance < [amount].
     * [refId] ties both ledger entries to an external entity (wagerId, withdrawalId, etc.)
     */
    suspend fun transfer(
        fromUserId: String,
        toUserId: String,
        amount: Long,
        currency: CurrencyType,
        type: LedgerEntryType,
        refId: String,
    )

    /**
     * Credits [userId] wallet. Called after confirming an external deposit.
     */
    suspend fun deposit(
        userId: String,
        amount: Long,
        currency: CurrencyType,
        externalRef: String,
    )
}