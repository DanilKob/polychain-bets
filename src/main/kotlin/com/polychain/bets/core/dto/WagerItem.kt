package com.polychain.bets.core.dto

import com.polychain.bets.core.entity.WagerStatus
import java.time.Instant

data class WagerItem(
    val id: String,
    val name: String,
    val text: String,
    val mediaId: String,
    val answerMediaId: String,
    val status: WagerStatus,
    val createdAt: Instant
)
