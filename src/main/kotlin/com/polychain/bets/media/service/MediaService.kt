package com.polychain.bets.media.service

import com.polychain.bets.core.dto.MediaDto
import com.polychain.bets.core.entity.WagerMediaEntity
import com.polychain.bets.core.repository.WagerMediaMongoRepository
import com.polychain.bets.exception.NotFoundException
import com.polychain.bets.media.entity.VideoQuality
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class MediaService(
    private val videoStorageService: VideoStorageServiceInterface,
    private val contentPresignService: ContentPresignServiceInterface,
    private val wagerMediaMongoRepository: WagerMediaMongoRepository,
    private val videoTokenService: VideoTokenServiceInterface,
): MediaServiceInterface {

    override suspend fun getMasterManifestByVideoId(videoId: String): String {
        checkWagerEntityExistByVideoId(videoId)
        return videoStorageService.getMasterManifestAsStringByVideoId(videoId)
    }

    override suspend fun getQualityManifestByVideoId(
        videoId: String,
        quality: String,
    ): String {
        val quality = VideoQuality.fromValue(quality) ?: throw IllegalArgumentException("Quality not valid")
        checkWagerEntityExistByVideoId(videoId)

        val raw = videoStorageService.getQualityManifestAsStringByVideoId(
            videoId = videoId,
            quality = quality
        )

        return contentPresignService.presignQualityManifest(
            manifest = raw,
            videoId = videoId,
            quality = quality.value
        )
    }

    override fun mapEntityToDto(mediaEntity: WagerMediaEntity): MediaDto {
        return MediaDto(
            id = mediaEntity.id,
            videoId = mediaEntity.video.videoId,
            videoToken = videoTokenService.issueToken(mediaEntity.video.videoId).token,
            originalFilename = mediaEntity.video.originalFilename,
            hlsUrl = contentPresignService.getVideoMasterManifestUrl(mediaEntity.video.videoId),
            thumbnailUrl = mediaEntity.video.thumbnailS3Key
                .let { contentPresignService.presignS3Key(
                    s3Key = it
                ) },
            previewUrl = mediaEntity.video.previewS3Key
                .let { contentPresignService.presignS3Key(
                    s3Key = it
                ) },
            qualities = mediaEntity.video.qualities,
            durationSeconds = mediaEntity.video.durationSeconds,
            width = mediaEntity.video.width,
            height = mediaEntity.video.height,
        )
    }

    override fun getSupportedQualities(): List<VideoQuality> {
        return listOf(
            VideoQuality.QUALITY_360,
            VideoQuality.QUALITY_720,
            VideoQuality.QUALITY_1080
        )
    }

    private suspend fun getWagerMediaEntityByVideoId(videoId: String): WagerMediaEntity {
        val wagerMediaEntity = wagerMediaMongoRepository.findByVideoVideoId(videoId).awaitSingleOrNull()
            ?: throw NotFoundException("Wager not found")
        return wagerMediaEntity
    }

    private suspend fun checkWagerEntityExistByVideoId(videoId: String): Long {
        return wagerMediaMongoRepository.countByVideoVideoId(videoId).awaitSingleOrNull() ?: 0L
    }
}