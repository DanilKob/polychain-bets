package com.polychain.bets.media.service

interface ContentPresignServiceInterface {
    fun presignQualityManifest(
        manifest: String,
        videoId: String,
        quality: String
    ): String

    fun getVideoMasterManifestUrl(videoId: String): String
    fun presignS3Key(s3Key: String): String
}