package com.polychain.bets.config

import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import mu.KotlinLogging
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.jsr310.Jsr310CodecProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@Configuration
@EnableReactiveMongoRepositories(basePackages = [
    "com.polychain.bets.core",
    "com.polychain.bets.media"
])
@EnableReactiveMongoAuditing
class MongoConfiguration(
    @Value("\${mongodb.ssl.enabled}")
    private val sslEnabled: Boolean,
    @Value("\${mongodb.ssl.cert}")
    private val certificateFileName: String,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Customizes the auto-configured MongoClient with codec registry, UUID representation,
     * Stable API version, and optional SSL. Spring Boot picks up the URI from spring.mongodb.uri.
     */
    @Bean
    fun mongoClientSettingsCustomizer(): MongoClientSettingsBuilderCustomizer =
        MongoClientSettingsBuilderCustomizer { builder ->
            // Jsr310CodecProvider adds native BSON codec support for java.time types
            // (Instant, LocalDate, LocalDateTime, etc.). Instant maps to BSON Date.
            val codecRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(Jsr310CodecProvider()),
                MongoClientSettings.getDefaultCodecRegistry()
            )
            builder
                .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(codecRegistry)

            if (sslEnabled) {
                logger.info("MongoDB SSL enabled, loading certificate: {}", certificateFileName)
                val certStream = javaClass.classLoader.getResourceAsStream(certificateFileName)
                    ?: error("MongoDB certificate file not found on classpath: $certificateFileName")
                builder.applyToSslSettings { ssl ->
                    ssl.enabled(true).context(createSSLContext(certStream))
                }
            }
        }

    /**
     * Registers a ReactiveMongoTransactionManager backed by a single-node replica set.
     *
     * MongoDB requires a replica set to support multi-document transactions.
     * The docker-compose setup starts MongoDB in replica set mode (rs0) for this purpose.
     *
     * Note: all status updates in this service touch a single document, which MongoDB
     * guarantees atomically without an explicit transaction. The manager is registered
     * here so @Transactional works correctly if multi-document operations are added later.
     */
    @Bean
    fun transactionManager(factory: ReactiveMongoDatabaseFactory): ReactiveMongoTransactionManager =
        ReactiveMongoTransactionManager(factory)

    private fun createSSLContext(certFileInputStream: InputStream): SSLContext {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        certFileInputStream.use { fis ->
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificates = certificateFactory.generateCertificates(fis)
            certificates.forEachIndexed { index, certificate ->
                if (certificate is X509Certificate) {
                    keyStore.setCertificateEntry("aws-cert-$index", certificate)
                }
            }
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        return sslContext
    }
}