package com.polychain.bets.media.service

import com.polychain.bets.media.entity.VideoQuality
import java.io.File

interface VideoStorageServiceInterface {
    fun generatePresignedUrl(
        s3Key: String,
        expiryHours: Long = 2
    ): String

    fun uploadFile(
        localFile: File,
        s3Key: String,
        contentType: String
    ): String
    fun uploadDirectory(localDir: File, s3Prefix: String)
    fun deleteObject(s3Key: String)
    fun getObjectAsString(s3Key: String): String
    fun downloadFile(s3Key: String, targetFile: File)
    fun getMasterManifestAsStringByVideoId(videoId: String): String
    fun getQualityManifestAsStringByVideoId(videoId: String, quality: VideoQuality): String
}