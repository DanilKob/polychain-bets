package com.polychain.bets.auth.filter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.polychain.bets.auth.entity.FirebaseJwt
import com.polychain.bets.auth.entity.FirebasePrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper

@Component
class FirebaseAuthFilter(
    private val firebaseAuth: FirebaseAuth,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {
    private val logger = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        // Internal routes are protected by X-Internal-Secret, not Firebase tokens
        if (request.requestURI.startsWith("/firebase/")) {
            chain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val idToken = authHeader.removePrefix("Bearer ").trim()
            try {
                val decodedToken = firebaseAuth.verifyIdToken(idToken)
                val jwt = objectMapper.convertValue(decodedToken.claims, FirebaseJwt::class.java)
                val principal = FirebasePrincipal(
                    uid = decodedToken.uid,
                    email = jwt.email,
                    name = jwt.name,
                    emailVerified = jwt.emailVerified,
                    picture = jwt.picture,
                    iss = decodedToken.issuer,
                    aud = jwt.aud,
                    iat = jwt.iat,
                    exp = jwt.exp,
                    authTime = jwt.authTime,
                    provider = jwt.firebase.signInProvider,
                    identities = jwt.firebase.identities
                )
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(principal, null, emptyList())

            } catch (e: FirebaseAuthException) {
                logger.error(e) { "Firebase token verification failed" }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
                return
            }
        }

        chain.doFilter(request, response)
    }
}
