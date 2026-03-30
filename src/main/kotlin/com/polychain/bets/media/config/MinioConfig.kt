package com.polychain.bets.media.config

import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "minio")
class MinioConfig {
    var endpoint: String = "http://localhost:9000"
    var accessKey: String = "minioadmin"
    var secretKey: String = "minioadmin"
    var bucket: String = "videos"
    var expiryHours: Long = 2

    @Bean
    fun minioClient(): MinioClient = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build()
}