package com.polychain.bets.core.dto

import com.polychain.bets.core.entity.WagerStatus
import java.time.Instant

data class WagerDto(
    val id: String,
    val name: String,
    val text: String,
    val status: WagerStatus,

    val media: MediaDto,

    val outcomes: List<WagerOutcomeDto>,

    val createdAt: Instant = Instant.now(),
)
