package com.polychain.bets.auth.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the `firebase` nested claim inside a Firebase ID token.
 *
 * Example decoded JWT:
 * {
 *   "firebase": {
 *     "sign_in_provider": "google.com",
 *     "identities": {
 *       "google.com": ["105577728002694059667"],
 *       "email":      ["kobzardanila@gmail.com"]
 *     }
 *   }
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FirebaseJwtClaim(
    @JsonProperty("sign_in_provider")
    val signInProvider: String,                       // "google.com" | "apple.com" | "password" | "phone"

    val identities: Map<String, List<String>> = emptyMap()  // provider → list of provider UIDs / emails
)