package com.polychain.bets.media.service

import com.polychain.bets.media.config.MinioConfig
import org.springframework.stereotype.Service

const val VIDEO_API_PREFIX = "api/v1/video"

@Service
class ContentPresignService(
    private val storageService: VideoStorageServiceInterface,
    private val minioConfig: MinioConfig
) : ContentPresignServiceInterface {

    override fun presignQualityManifest(
        manifest: String,
        videoId: String,
        quality: String,
    ): String {
        return manifest.lines().joinToString("\n") { line ->
            if (line.isNotBlank() && !line.startsWith("#") && line.endsWith(".ts")) {
                storageService.generatePresignedUrl("video_$videoId/$quality/$line", minioConfig.expiryHours)
            } else {
                line
            }
        }
    }

    override fun getVideoMasterManifestUrl(videoId: String): String {
        return "/$VIDEO_API_PREFIX/$videoId/hls/master.m3u8"
    }

    override fun presignS3Key(s3Key: String): String {
        return storageService.generatePresignedUrl(
            s3Key = s3Key,
            expiryHours = minioConfig.expiryHours
        )
    }
}