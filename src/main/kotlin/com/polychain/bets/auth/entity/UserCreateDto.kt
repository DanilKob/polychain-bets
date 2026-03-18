package com.polychain.bets.auth.entity

import org.bson.Document

data class UserCreateDto(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val emailVerified: Boolean,
    val photoURL: String?,
    val disabled: Boolean,
    val provider: String,
    val registrationEvent: Document? = null
)
