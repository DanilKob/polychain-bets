package com.polychain.bets.core.service

import com.polychain.bets.app.dto.CreateWagerRequest
import com.polychain.bets.core.dto.CursorParams
import com.polychain.bets.core.dto.MappingUtils.toDto
import com.polychain.bets.core.dto.WagerDto
import com.polychain.bets.core.dto.WagerItem
import com.polychain.bets.core.entity.VideoData
import com.polychain.bets.core.entity.WagerEntity
import com.polychain.bets.core.entity.WagerMediaEntity
import com.polychain.bets.core.entity.WagerOutcomeEntity
import com.polychain.bets.core.entity.WagerStatus
import com.polychain.bets.core.repository.WagerMediaMongoRepository
import com.polychain.bets.core.repository.WagerMongoRepository
import com.polychain.bets.core.repository.WagerOutcomeMongoRepository
import com.polychain.bets.media.repository.VideoRepository
import com.polychain.bets.media.service.MediaServiceInterface
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class WagerService(
    private val wagerRepository: WagerMongoRepository,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
    private val wagerOutcomeMongoRepository: WagerOutcomeMongoRepository,
    private val wagerMediaMongoRepository: WagerMediaMongoRepository,
    private val mediaService: MediaServiceInterface,
    private val videoRepository: VideoRepository,
) : WagerServiceInterface {

    companion object {
        private val FEED_STATUSES = listOf(
            WagerStatus.ACTIVE,
            WagerStatus.VOTE_CLOSED,
            WagerStatus.FINISH_PROCESSING_STARTED,
            WagerStatus.FINISH_WINNERS_CALCULATED,
            WagerStatus.FINISH_REWARD_DISTRIBUTION_STARTED,
            WagerStatus.FINISHED,
        )
    }

    override suspend fun searchForFeed(
        tags: List<String>?,
        cursor: CursorParams?,
        limit: Int,
    ): List<WagerDto> = coroutineScope {
        val wagers = queryWagers(
            tags = tags,
            cursor = cursor,
            limit = limit
        )
        if (wagers.isEmpty()) return@coroutineScope emptyList()

        val wagerIds = wagers.map { it.id }
        val mediaIds = wagers.map { it.mediaId }

        val outcomesDeferred = async {
            getOutcomesByWagerIds(wagerIds)
        }
        val mediaDeferred = async {
            getWagerMediaByWagerIds(mediaIds)
        }

        val outcomesByWagerId = outcomesDeferred.await()
        val mediaById = mediaDeferred.await()

        return@coroutineScope wagers.mapNotNull { wager ->
            val media = mediaById[wager.mediaId] ?: return@mapNotNull null
            wager.toDto(
                outcomes = outcomesByWagerId[wager.id] ?: emptyList(),
                media = mediaService.mapEntityToDto(media),
            )
        }
    }

    override suspend fun searchItemsForFeed(
        tags: List<String>?,
        cursor: CursorParams?,
        limit: Int,
    ): List<WagerItem> {
        return queryWagers(
            tags = tags,
            cursor = cursor,
            limit = limit
        ).map {
            WagerItem(
                id = it.id,
                name = it.name,
                text = it.text,
                status = it.status,
                mediaId = it.mediaId,
                answerMediaId = it.answerMediaId,
                createdAt = it.createdAt
            )
        }
    }

    override suspend fun getOutcomesByWagerIds(wagerIds: List<String>): Map<String, List<WagerOutcomeEntity>> {
        return wagerOutcomeMongoRepository.findByWagerIdInAndDeletedFalse(wagerIds)
            .collectList()
            .map { list -> list.groupBy { it.wagerId } }
            .awaitSingle()
    }

    override suspend fun getWagerMediaByWagerIds(mediaIds: List<String>): Map<String, WagerMediaEntity?> {
        return wagerMediaMongoRepository.findAllById(mediaIds).collectList()
            .map { list -> list.associateBy { it.id} }
            .awaitSingle()
    }

    override suspend fun createDebugWagerForController(
        request: CreateWagerRequest
    ) {
        val video = videoRepository.findAll().awaitFirstOrNull() ?: throw IllegalStateException("No videos found")

        val wagerMediaEntity = wagerMediaMongoRepository.save(
            WagerMediaEntity(
                uploaderId = "system",
                video = VideoData(
                    videoId = video.id.toString(),
                    originalFilename = video.originalFilename,
                    rawS3Key = video.rawS3Key,
                    thumbnailS3Key = video.thumbnailS3Key,
                    manifestS3Key = video.manifestS3Key,
                    previewS3Key = video.previewS3Key,
                    qualities = mediaService.getSupportedQualities().map { it.value },
                    width = video.width,
                    height = video.height,
                    durationSeconds = video.durationSeconds
                )
            )
        ).awaitSingle()

        val wager = wagerRepository.save(
            WagerEntity(
                name = request.name,
                text = request.text,
                status = WagerStatus.INITIALIZING,
                mediaId = wagerMediaEntity.id,
                answerMediaId = wagerMediaEntity.id
            )
        ).awaitSingle()
        val wagerOutcomeEntities = request.outcomes.map { outcome ->
            WagerOutcomeEntity(
                wagerId = wager.id,
                description = outcome.description,
            )
        }
        wagerOutcomeMongoRepository.saveAll(wagerOutcomeEntities).collectList().awaitSingle()
        wager.status = WagerStatus.ACTIVE
        wagerRepository.save(wager).awaitSingle()
    }

    override suspend fun createWagersForController(n: Int) {
        val videos = videoRepository.findAll().collectList().awaitSingle()
        val videosCount = videos.size
        if (videos.isEmpty()) return

        for (i in 0 until n) {
            val video = videos[(i % videosCount)]
            val wagerMediaEntity = wagerMediaMongoRepository.save(
                WagerMediaEntity(
                    uploaderId = "system",
                    video = VideoData(
                        videoId = video.id.toString(),
                        originalFilename = video.originalFilename,
                        rawS3Key = video.rawS3Key,
                        thumbnailS3Key = video.thumbnailS3Key,
                        manifestS3Key = video.manifestS3Key,
                        previewS3Key = video.previewS3Key,
                        qualities = mediaService.getSupportedQualities().map { it.value },
                        width = video.width,
                        height = video.height,
                        durationSeconds = video.durationSeconds
                    )
                )
            ).awaitSingle()

            val wager = wagerRepository.save(
                WagerEntity(
                    name = "Wager $i",
                    text = "What will happen next? $i",
                    status = WagerStatus.INITIALIZING,
                    mediaId = wagerMediaEntity.id,
                    answerMediaId = wagerMediaEntity.id
                )
            ).awaitSingle()
            val wagerOutcomeEntities = listOf("Answer A", "Answer B").map { outcome ->
                WagerOutcomeEntity(
                    wagerId = wager.id,
                    description = outcome,
                )
            }
            wagerOutcomeMongoRepository.saveAll(wagerOutcomeEntities).collectList().awaitSingle()
            wager.status = WagerStatus.ACTIVE
            wagerRepository.save(wager).awaitSingle()
        }
    }

    private suspend fun queryWagers(
        tags: List<String>?,
        cursor: CursorParams?,
        limit: Int
    ): List<WagerEntity> {
        val conditions = mutableListOf(
            Criteria.where("deleted").`is`(false),
            Criteria.where("mediaId").ne(null),
            Criteria.where("status").`in`(FEED_STATUSES),
        )
        // Keyset pagination: (createdAt < cursor) OR (createdAt == cursor AND id < cursor.id)
        cursor?.let { cursor ->
            conditions.add(
                Criteria().orOperator(
                    Criteria.where("created_at").lt(cursor.createdAt),
                    Criteria.where("created_at").`is`(cursor.createdAt).and("_id").lt(cursor.videoId),
                )
            )
        }
        if (!tags.isNullOrEmpty()) {
            conditions.add(Criteria.where("tags").`in`(tags))
        }

        val query = Query(Criteria().andOperator(*conditions.toTypedArray()))
            .with(Sort.by(Sort.Direction.DESC, "created_at", "_id"))
            .limit(limit)

        return reactiveMongoTemplate.find(query, WagerEntity::class.java).collectList().awaitSingle()
    }
}