package com.polychain.bets.auth.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the decoded Firebase ID token claims.
 *
 * Standard JWT fields plus Firebase-specific claims.
 * Deserialize from FirebaseToken.claims using ObjectMapper.convertValue().
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FirebaseJwt(
    // User identity
    val sub: String,                          // Subject — same as uid
    @JsonProperty("user_id")
    val userId: String,                       // Firebase UID
    val email: String?,
    @JsonProperty("email_verified")
    val emailVerified: Boolean,
    val name: String?,
    val picture: String?,                     // Profile photo URL

    // Token metadata
    val iss: String,                          // Issuer: "https://securetoken.google.com/<project>"
    val aud: String,                          // Audience: Firebase project ID
    val iat: Long,                            // Issued at (Unix seconds)
    val exp: Long,                            // Expires at (Unix seconds)
    @JsonProperty("auth_time")
    val authTime: Long,                       // When the user last authenticated (Unix seconds)

    // Firebase-specific
    val firebase: FirebaseJwtClaim            // Sign-in provider + linked identities
)