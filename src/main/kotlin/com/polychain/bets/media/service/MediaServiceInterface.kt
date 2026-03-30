package com.polychain.bets.media.service

import com.polychain.bets.core.dto.MediaDto
import com.polychain.bets.core.entity.WagerMediaEntity
import com.polychain.bets.media.entity.VideoQuality

interface MediaServiceInterface {

    fun mapEntityToDto(mediaEntity: WagerMediaEntity): MediaDto
    /**
     * HLS manifest proxy — master playlist.
     *
     * Fetches master.m3u8 from MinIO and returns it unchanged. The quality playlist
     * references inside (e.g. "360p/playlist.m3u8") are relative paths; the HLS player
     * resolves them against this URL's base, which routes them to getQualityPlaylist()
     * below. No rewriting needed at this level.
     */
    suspend fun getMasterManifestByVideoId(videoId: String): String

    /**
     * HLS manifest proxy — quality playlist.
     *
     * Fetches the quality-specific playlist from MinIO and rewrites each .ts segment
     * filename to an absolute presigned MinIO URL. This is the key step that makes HLS
     * work with S3: the player needs absolute URLs for segments because presigned URLs
     * cannot be reconstructed from relative paths. Segment data flows directly from
     * MinIO to the player — the app only proxies these small text manifests.
     */
    suspend fun getQualityManifestByVideoId(videoId: String, quality: String): String
    fun getSupportedQualities(): List<VideoQuality>
}