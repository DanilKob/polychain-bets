package com.polychain.bets.media.service

import com.polychain.bets.exception.StorageException
import com.polychain.bets.media.config.MinioConfig
import com.polychain.bets.media.entity.VideoQuality
import io.minio.BucketExistsArgs
import io.minio.DownloadObjectArgs
import io.minio.GetObjectArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.RemoveObjectArgs
import io.minio.UploadObjectArgs
import io.minio.http.Method
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.TimeUnit

@Service
class MinioStorageService(
    private val minioClient: MinioClient,
    private val minioConfig: MinioConfig
) : VideoStorageServiceInterface {
    private val logger = KotlinLogging.logger {}

    init {
        ensureBucketExists()
    }

    override fun generatePresignedUrl(s3Key: String, expiryHours: Long): String {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.bucket)
                    .`object`(s3Key)
                    .expiry(expiryHours.toInt(), TimeUnit.HOURS)
                    .build()
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate presigned URL for $s3Key" }
            throw StorageException("Failed to generate presigned URL for $s3Key", e)
        }
    }

    override fun uploadFile(localFile: File, s3Key: String, contentType: String): String {
        try {
            logger.debug("Uploading {} to s3://{}/{}", localFile.name, minioConfig.bucket, s3Key)
            minioClient.uploadObject(
                UploadObjectArgs.builder()
                    .bucket(minioConfig.bucket)
                    .`object`(s3Key)
                    .filename(localFile.absolutePath)
                    .contentType(contentType)
                    .build()
            )
            return s3Key
        } catch (e: Exception) {
            logger.error(e) { "Failed to upload file to $s3Key" }
            throw StorageException("Failed to upload file to $s3Key", e)
        }
    }

    override fun uploadDirectory(localDir: File, s3Prefix: String) {
        localDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val relativePath = localDir.toURI().relativize(file.toURI()).path
                val s3Key = "$s3Prefix/$relativePath"
                val contentType = resolveContentType(file.extension)
                uploadFile(file, s3Key, contentType)
            }
    }

    override fun deleteObject(s3Key: String) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.bucket)
                    .`object`(s3Key)
                    .build()
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete object $s3Key" }
            throw StorageException("Failed to delete object $s3Key", e)
        }
    }

    override fun getMasterManifestAsStringByVideoId(videoId: String): String {
        return getObjectAsString("video_$videoId/master.m3u8")
    }

    override fun getQualityManifestAsStringByVideoId(
        videoId: String,
        quality: VideoQuality
    ): String {
        return getObjectAsString("video_$videoId/${quality.value}/playlist.m3u8")
    }

    override fun getObjectAsString(s3Key: String): String {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.bucket)
                    .`object`(s3Key)
                    .build()
            ).use { it.readBytes().toString(Charsets.UTF_8) }
        } catch (e: Exception) {
            logger.error(e) { "Failed to read object $s3Key" }
            throw StorageException("Failed to read object $s3Key", e)
        }
    }

    override fun downloadFile(s3Key: String, targetFile: File) {
        try {
            targetFile.parentFile?.mkdirs()
            minioClient.downloadObject(
                DownloadObjectArgs.builder()
                    .bucket(minioConfig.bucket)
                    .`object`(s3Key)
                    .filename(targetFile.absolutePath)
                    .build()
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to download $s3Key to ${targetFile.absolutePath}" }
            throw StorageException("Failed to download $s3Key to ${targetFile.absolutePath}", e)
        }
    }

    private fun ensureBucketExists() {
        try {
            val exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(minioConfig.bucket).build()
            )
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(minioConfig.bucket).build()
                )
                logger.info("Created MinIO bucket: {}", minioConfig.bucket)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize MinIO bucket '${minioConfig.bucket}'" }
            throw StorageException("Failed to initialize MinIO bucket '${minioConfig.bucket}'", e)
        }
    }

    private fun resolveContentType(extension: String): String = when (extension.lowercase()) {
        "m3u8" -> "application/x-mpegURL"
        "ts"   -> "video/MP2T"
        "mp4"  -> "video/mp4"
        "jpg", "jpeg" -> "image/jpeg"
        "png"  -> "image/png"
        else   -> "application/octet-stream"
    }
}