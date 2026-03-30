package com.polychain.bets.core.entity

import org.bson.Document
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.Document as MongoDocument
import java.time.Instant
import java.util.UUID

@MongoDocument(collection = "users")
data class UserEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Field("uid")
    val uid: String,
    @Field("email")
    val email: String?,
    @Field("email_verified")
    val emailVerified: Boolean,
    @Field("display_name")
    val displayName: String?,
    @Field("photo_URL")
    val photoURL: String?,
    @Field("disabled")
    val disabled: Boolean,
    @Field("provider")
    val provider: String,      // "google.com" | "apple.com" | "password"
    @Field("registration_event")
    val registrationEvent: Document? = null,  // raw Firebase beforeUserCreated event (credential/passwordHash stripped)

    @CreatedDate
    @Field("created_at")
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Field("updated_at")
    val updatedAt: Instant = Instant.now(),
    @Field("deleted")
    var deleted: Boolean = false
)