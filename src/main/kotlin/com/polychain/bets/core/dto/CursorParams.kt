package com.polychain.bets.core.dto

import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant
import java.util.Base64

data class CursorParams(
    val videoId: String,
    val createdAt: Instant,
) {
    companion object {
        private val mapper = jacksonObjectMapper()

        fun encode(cursor: CursorParams): String {
            val json = mapper.writeValueAsString(cursor)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())
        }

        fun decode(encoded: String): CursorParams {
            val json = Base64.getUrlDecoder().decode(encoded).toString(Charsets.UTF_8)
            return mapper.readValue(json, CursorParams::class.java)
        }
    }
}
