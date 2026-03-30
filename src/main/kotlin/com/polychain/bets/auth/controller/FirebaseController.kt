package com.polychain.bets.auth.controller

import com.polychain.bets.auth.entity.UserCreateDto
import com.polychain.bets.auth.service.UserService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.bson.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode

private val logger = KotlinLogging.logger {}

/**
 * Called exclusively by the Firebase beforeCreate blocking function.
 * Protected by a shared secret — never exposed to clients.
 */
@RestController
@RequestMapping("/firebase")
class FirebaseController(
    private val userService: UserService,
    @Value("\${internal.secret}") private val internalSecret: String
) {

    @PostMapping("/users")
    fun createUser(
        @RequestHeader("X-Internal-Secret") secret: String?,
        @RequestBody event: JsonNode
    ): ResponseEntity<Unit> = runBlocking {
        if (secret != internalSecret) {
            logger.warn { "Rejected /firebase/users call with invalid secret" }
            return@runBlocking ResponseEntity.status(401).build()
        }
        check (event.has("data"), {"User data missing"})
        val user = event["data"]
        logger.info { event.toPrettyString() }
        userService.findOrCreate(
            UserCreateDto(
                uid = user["uid"].asString(),
                email = user["email"]?.asString(),
                displayName = user["displayName"]?.asString(),
                emailVerified = user["emailVerified"]?.asBoolean() ?: false,
                photoURL = user["photoURL"]?.asString(),
                disabled = user["disabled"]?.asBoolean() ?: false,
                provider = user["providerData"]?.firstOrNull()?.get("providerId")?.asString() ?: "unknown",
                registrationEvent = Document.parse(event.toString())
            )
        )

        return@runBlocking ResponseEntity.ok().build()
    }
}
