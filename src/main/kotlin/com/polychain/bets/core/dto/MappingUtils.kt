package com.polychain.bets.core.dto

import com.polychain.bets.core.entity.WagerEntity
import com.polychain.bets.core.entity.WagerOutcomeEntity

object MappingUtils {

    fun WagerEntity.toDto(outcomes: List<WagerOutcomeEntity>, media: MediaDto) = WagerDto(
        id = id,
        name = name,
        text = text,
        status = status,
        media = media,
        outcomes = outcomes.map { it.toDto() },
        createdAt = createdAt,
    )

    fun WagerOutcomeEntity.toDto() = WagerOutcomeDto(
        id = id,
        wagerId = wagerId,
        description = description,
    )
}