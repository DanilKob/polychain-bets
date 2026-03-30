package com.polychain.bets.media.service

interface ManifestRewriterInterface {
    /**
     * Rewrites master.m3u8 — quality playlist paths point to your backend
     *
     * Before:
     *   720p/playlist.m3u8
     *
     * After:
     *   https://api.example.com/api/videos/abc123/manifest/720p/playlist.m3u8
     */
    fun rewriteMasterPlaylist(raw: String, videoId: String): String

    /**
     * Rewrites playlist.m3u8 — segment paths become presigned S3 URLs
     *
     * Before:
     *   seg_000.ts
     *
     * After:
     *   https://minio.example.com/videos/video_abc123/720p/seg_000.ts?X-Amz-Signature=...
     */
    fun rewritePlaylist(raw: String, videoId: String, quality: String): String
}