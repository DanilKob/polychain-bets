package com.polychain.bets.media.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ManifestRewriter(
    @Value("\${app.base-url}") private val baseUrl: String,
    private val videoStorageService: VideoStorageServiceInterface,
) : ManifestRewriterInterface {

    /**
     * Rewrites master.m3u8 — quality playlist paths point to your backend
     *
     * Before:
     *   720p/playlist.m3u8
     *
     * After:
     *   https://api.example.com/api/v1/video/abc123/hls/720p/playlist.m3u8
     */
    override fun rewriteMasterPlaylist(raw: String, videoId: String): String {
        return raw.lines().joinToString("\n") { line ->
            when {
                line.startsWith("#") -> line
                line.endsWith(".m3u8") -> {
                    val quality = line.removeSuffix("/playlist.m3u8")   // "720p"
                    "$baseUrl/api/v1/video/$videoId/hls/$quality/playlist.m3u8"
                }
                else -> line
            }
        }
    }

    /**
     * Rewrites playlist.m3u8 — segment paths become presigned S3 URLs
     *
     * Before:
     *   seg_000.ts
     *
     * After:
     *   https://minio.example.com/videos/video_abc123/720p/seg_000.ts?X-Amz-Signature=...
     */
    override fun rewritePlaylist(raw: String, videoId: String, quality: String): String {
        return raw.lines().joinToString("\n") { line ->
            when {
                line.startsWith("#") -> line
                line.endsWith(".ts") -> {
                    val s3Key = "video_$videoId/$quality/$line"   // "video_abc/720p/seg_000.ts"
                    videoStorageService.generatePresignedUrl(s3Key, expiryHours = 2)
                }
                line.isBlank() -> line
                else -> line
            }
        }
    }
}