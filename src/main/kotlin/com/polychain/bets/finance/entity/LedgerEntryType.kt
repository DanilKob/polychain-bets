package com.polychain.bets.finance.entity

enum class LedgerEntryType {
    DEPOSIT,
    WITHDRAWAL,
    BET_PLACED,  // user → platform when bet placed
    BET_WIN,     // platform → user when wager won
    BET_REFUND,  // platform → user when wager cancelled
}