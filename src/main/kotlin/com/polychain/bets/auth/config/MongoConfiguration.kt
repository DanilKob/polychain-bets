package com.polychain.bets.auth.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@Configuration
@EnableMongoRepositories(basePackages = ["com.polychain.bets.core"])
class MongoConfiguration(
    @Value("\${mongodb.ssl.enabled}")
    private val sslEnabled: Boolean,
    @Value("\${spring.mongodb.uri}")
    private val mongoUri: String,
    @Value("\${mongodb.ssl.cert}")
    private val certificateFileName: String,
    @Value("\${mongodb.database}")
    private val databaseName: String
) : AbstractMongoClientConfiguration() {

    private val logger = KotlinLogging.logger {}

    override fun getDatabaseName(): String {
        val connectionString = ConnectionString(mongoUri)
        return connectionString.database?: databaseName
    }

    @Bean
    override fun mongoClient(): MongoClient {
        val connectionString = ConnectionString(mongoUri)
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .serverApi(serverApi)
            .build()
        // Create a new client and connect to the server
        return MongoClients.create(mongoClientSettings)
    }

    @Bean
    fun mongoTemplate(): MongoTemplate {
        return MongoTemplate(mongoClient(), databaseName)
    }

    fun createSSLContext(certFileInputStream: InputStream): SSLContext {
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