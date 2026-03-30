package com.polychain.bets.media.service

import com.polychain.bets.exception.VideoTokenInvalidException
import com.polychain.bets.media.dto.VideoToken
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class VideoTokenService(
    @Value("\${video.token.secret}") private val secret: String,
    @Value("\${video.token.expiry-seconds}") private val expirySeconds: Long,
) : VideoTokenServiceInterface {
    private val logger = KotlinLogging.logger {}

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }

    override fun issueToken(videoId: String): VideoToken {
        val token = Jwts.builder()
            .subject(videoId)
            .expiration(Date.from(Instant.now().plusSeconds(expirySeconds)))
            .signWith(key)
            .compact()
        return VideoToken(token = token, expirySeconds = expirySeconds)
    }

    override fun validateToken(videoId: String, token: String) {
        val subject = try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: JwtException) {
            logger.error(e) { "JWT validation failed for videoId=$videoId" }
            throw VideoTokenInvalidException("Invalid or expired video token")
        }
        if (subject != videoId) {
            throw VideoTokenInvalidException("Token is not valid for this video")
        }
    }
}