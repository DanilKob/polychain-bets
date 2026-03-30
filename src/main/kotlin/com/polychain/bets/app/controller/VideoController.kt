package com.polychain.bets.app.controller

import com.polychain.bets.auth.entity.FirebasePrincipal
import com.polychain.bets.exception.VideoTokenInvalidException
import com.polychain.bets.media.dto.VideoToken
import com.polychain.bets.media.service.MediaServiceInterface
import com.polychain.bets.media.service.VideoTokenServiceInterface
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/video")
class VideoController(
    private val mediaService: MediaServiceInterface,
    private val videoTokenService: VideoTokenServiceInterface,
) {

    @GetMapping("/{id}/token")
    fun getVideoToken(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: FirebasePrincipal?,
    ): ResponseEntity<VideoToken> {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(videoTokenService.issueToken(id.toString()))
    }

    @GetMapping("/{id}/hls/master.m3u8", produces = ["application/x-mpegURL"])
    fun getMasterManifest(
        @PathVariable id: UUID,
        @RequestParam vt: String,
    ): ResponseEntity<String> = runBlocking {
        videoTokenService.validateToken(id.toString(), vt)
        val manifest = mediaService.getMasterManifestByVideoId(id.toString())
        ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .body(manifest)
    }

    @GetMapping("/{id}/hls/{quality}/playlist.m3u8", produces = ["application/x-mpegURL"])
    fun getQualityPlaylist(
        @PathVariable id: UUID,
        @PathVariable quality: String,
        @RequestParam vt: String,
    ): ResponseEntity<String> = runBlocking {
        videoTokenService.validateToken(id.toString(), vt)
        val manifest = mediaService.getQualityManifestByVideoId(id.toString(), quality)
        ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .body(manifest)
    }

    @ExceptionHandler(VideoTokenInvalidException::class)
    fun handleInvalidToken(e: VideoTokenInvalidException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
}