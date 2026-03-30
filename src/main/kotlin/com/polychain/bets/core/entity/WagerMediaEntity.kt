package com.polychain.bets.core.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.util.*

@Document(collection = "wager_media")
data class WagerMediaEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Field("uploader_id")
    val uploaderId: String,

    @Field("video")
    val video: VideoData,

    @CreatedDate
    @Field("created_at")
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    @Field("updated_at")
    val updatedAt: Instant = Instant.now(),
    @Field("deleted")
    var deleted: Boolean = false
)

data class VideoData(
    @Field("video_id")
    val videoId: String,
    @Field("original_filename")
    val originalFilename: String,

    @Field("raw_s3_key")
    val rawS3Key: String,
    @Field("manifest_s3_key")
    val manifestS3Key: String,
    @Field("thumbnail_s3_key")
    val thumbnailS3Key: String,
    @Field("preview_s3_key")
    val previewS3Key: String,

    @Field("qualities")
    val qualities: List<String>,
    @Field("duration_seconds")
    val durationSeconds: Int,
    @Field("width")
    val width: Int,
    @Field("height")
    val height: Int,
)
