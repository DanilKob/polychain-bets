package com.polychain.bets.auth.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.InputStream


const val FIREBASE_SERVICE_ACCOUNT_JSON_ENV_KEY = "FIREBASE_SERVICE_ACCOUNT_JSON"
const val FIREBASE_SERVICE_ACCOUNT_JSON_PATH_ENV_KEY = "FIREBASE_SERVICE_ACCOUNT_JSON_PATH"

@Configuration
class FirebaseConfig {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isNotEmpty()) return FirebaseApp.getInstance()

        val jsonPath: String? = System.getenv(FIREBASE_SERVICE_ACCOUNT_JSON_PATH_ENV_KEY)
        val json: String? = System.getenv(FIREBASE_SERVICE_ACCOUNT_JSON_ENV_KEY)

        check (jsonPath != null || json != null, { "Missing Firebase service account JSON or Json path environment variables" })

        val credentialsInputStream = openCredentialsInputStream(jsonPath, json)

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(credentialsInputStream))
            .build()

        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseAuth(app: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(app)

    private fun openCredentialsInputStream(jsonPath: String?, json: String?): InputStream {
        return when {
            jsonPath != null -> {
                logger.info { "Opening Firebase service account JSON from path: $jsonPath" }
                FileInputStream(jsonPath)
            }
            json != null -> {
                logger.info { "Opening Firebase service account JSON from string" }
                json.byteInputStream()
            }
            else -> error("Missing Firebase service account JSON or Json path environment variables")
        }
    }
}
