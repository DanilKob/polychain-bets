package com.polychain.bets.auth.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class FirebasePrincipal(
    // User identity
    val uid: String,
    val email: String?,
    val name: String?,
    val emailVerified: Boolean,
    val picture: String?,

    // Token metadata
    val iss: String,                          // Issuer: "https://securetoken.google.com/<project>"
    val aud: String,                          // Audience: Firebase project ID
    val iat: Long,                            // Issued at (Unix seconds)
    val exp: Long,                            // Expires at (Unix seconds)
    @JsonProperty("auth_time")
    val authTime: Long,                       // When the user last authenticated (Unix seconds)

    val provider: String,                     // "google.com" | "apple.com" | "password" | "phone"
    val identities: Map<String, List<String>> = emptyMap()  // provider → list of provider UIDs / emails
)

