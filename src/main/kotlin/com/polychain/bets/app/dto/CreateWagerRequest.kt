package com.polychain.bets.app.dto

data class CreateWagerRequest(
    val name: String,
    val text: String,
    val outcomes: List<CreateWagerRequestItem>
)

data class CreateWagerRequestItem(
    val description: String
)
