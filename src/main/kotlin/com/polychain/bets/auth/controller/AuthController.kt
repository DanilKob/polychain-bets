package com.polychain.bets.auth.controller

import com.polychain.bets.auth.entity.FirebasePrincipal
import com.polychain.bets.auth.entity.UserCreateDto
import com.polychain.bets.auth.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class UserResponse(val uid: String, val email: String?, val displayName: String?)

@RestController
@RequestMapping("/auth")
class AuthController(private val userService: UserService) {

    /**
     * Called by the client after Firebase Auth completes.
     * Requires: Authorization: Bearer <firebase-id-token>
     *
     * For new users, the beforeCreate blocking function already saved them to the DB.
     * This endpoint performs an upsert as a fallback (e.g. if the function failed)
     * and returns the user's profile.
     */
    @PostMapping("/signin")
    fun signIn(@AuthenticationPrincipal principal: FirebasePrincipal): ResponseEntity<UserResponse> {

        val user = userService.findOrCreate(
            UserCreateDto(
                uid = principal.uid,
                email = principal.email,
                displayName = principal.name,
                emailVerified = principal.emailVerified,
                photoURL = principal.picture,
                disabled = false,
                provider = principal.provider
            )
        )

        return ResponseEntity.ok(UserResponse(user.uid, user.email, user.displayName))
    }
}
