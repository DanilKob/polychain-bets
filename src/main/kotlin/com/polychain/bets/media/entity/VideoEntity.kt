package com.polychain.bets.media.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = "videos")
class VideoEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    var originalFilename: String,

    @Indexed
    var status: VideoStatus = VideoStatus.PENDING,

    var processingMode: String,

    var rawS3Key: String,
    var manifestS3Key: String,
    var thumbnailS3Key: String,
    var previewS3Key: String,

    var durationSeconds: Int,
    var width: Int,
    var height: Int,

    @Indexed
    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now(),
    var processedAt: Instant? = null
)