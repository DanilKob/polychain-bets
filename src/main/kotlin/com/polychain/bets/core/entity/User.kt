package com.polychain.bets.core.entity

import org.bson.Document
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document as MongoDocument
import java.time.Instant
import java.util.UUID

@MongoDocument(collection = "users")
data class User(
    @Id
    var id: String = UUID.randomUUID().toString(),
    val uid: String,
    val email: String?,
    val emailVerified: Boolean,
    val displayName: String?,
    val photoURL: String?,
    val disabled: Boolean,
    val provider: String,      // "google.com" | "apple.com" | "password"
    val createdAt: Instant,
    val registrationEvent: Document? = null  // raw Firebase beforeUserCreated event (credential/passwordHash stripped)
)