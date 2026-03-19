package com.polychain.bets.auth.service

import com.polychain.bets.auth.entity.UserCreateDto
import com.polychain.bets.core.entity.User
import com.polychain.bets.core.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class UserService(private val userRepository: UserRepository) {

    fun findByUid(uid: String): User? {
        return userRepository.findByUid(uid)
    }

    fun findOrCreate(
        userCreateDto: UserCreateDto
    ): User {
        val uid = userCreateDto.uid
        val provider = userCreateDto.provider
        val existing = userRepository.findByUid(uid)
        if (existing != null) {
            // Apple only sends displayName on first registration (via beforeCreate blocking function).
            // If it arrives later via /auth/signin fallback, persist it.
            if (provider == "apple.com" && existing.displayName == null && userCreateDto.displayName != null) {
                return userRepository.save(existing.copy(displayName = userCreateDto.displayName))
            }
            return existing
        }
        logger.info { "Creating new user uid=$uid provider=$provider" }
        return userRepository.save(
            User(
                uid = uid,
                email = userCreateDto.email,
                displayName = userCreateDto.displayName,
                emailVerified = userCreateDto.emailVerified,
                photoURL = userCreateDto.photoURL,
                disabled = userCreateDto.disabled,
                provider = provider,
                createdAt = Instant.now(),
                registrationEvent = userCreateDto.registrationEvent
            )
        )
    }
}