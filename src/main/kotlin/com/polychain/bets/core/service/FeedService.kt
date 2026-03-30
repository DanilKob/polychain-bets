package com.polychain.bets.core.service

import com.polychain.bets.core.dto.CursorParams
import com.polychain.bets.core.dto.FeedItem
import com.polychain.bets.core.dto.FeedResponse
import com.polychain.bets.core.dto.MappingUtils.toDto
import com.polychain.bets.core.dto.WagerItem
import com.polychain.bets.core.entity.WagerMediaEntity
import com.polychain.bets.core.entity.WagerOutcomeEntity
import com.polychain.bets.media.service.MediaServiceInterface
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class FeedService(
    private val wagerService: WagerServiceInterface,
    private val mediaService: MediaServiceInterface,
) : FeedServiceInterface {

    private val logger = KotlinLogging.logger {}

    override suspend fun getFeed(
        tags: List<String>?,
        encodedCursor: String?,
        limit: Int,
    ): FeedResponse = coroutineScope {
        val pageSize = limit.coerceIn(1, MAX_PAGE_SIZE)
        val fetchSize = pageSize + 1

        val wagers = wagerService.searchItemsForFeed(
            tags = tags,
            cursor = encodedCursor?.let { CursorParams.decode(it) },
            limit = fetchSize,
        )

        val hasMore = wagers.size > pageSize
        val pageWagers = if (hasMore) wagers.dropLast(1) else wagers

        if (pageWagers.isEmpty()) {
            return@coroutineScope FeedResponse(items = emptyList(), nextCursor = null, hasMore = false, count = 0)
        }

        val wagerIds = pageWagers.map { it.id }
        val mediaIds = pageWagers.map { it.mediaId } + pageWagers.map { it.answerMediaId }
        val outcomesAsync = async {
            wagerService.getOutcomesByWagerIds(wagerIds)
        }
        val mediaAsync = async {
            wagerService.getWagerMediaByWagerIds(mediaIds)
        }
        val nextCursor = if (hasMore) {
            val last = pageWagers.last()
            CursorParams.encode(CursorParams(createdAt = last.createdAt, videoId = last.id))
        } else null

        val items = buildFeedItems(
            wagerItems = pageWagers,
            mediaAsync = mediaAsync,
            outcomesAsync = outcomesAsync
        )
        return@coroutineScope FeedResponse(
            items = items,
            nextCursor = nextCursor,
            hasMore = hasMore,
            count = items.size
        )
    }

    private suspend fun buildFeedItems(
        wagerItems: List<WagerItem>,
        mediaAsync: Deferred<Map<String, WagerMediaEntity?>>,
        outcomesAsync: Deferred<Map<String, List<WagerOutcomeEntity>>>
    ): List<FeedItem> {
        val mediaByWagerIdMap = mediaAsync.await()
        val outcomeByWagerIdMap = outcomesAsync.await()
        val items = wagerItems.mapNotNull { wagerItem ->
            val media = mediaByWagerIdMap[wagerItem.mediaId]
                ?.let { mediaService.mapEntityToDto(it) }
            if (media == null) {
                logger.warn { "Media not found for wager id=${wagerItem.id}" }
                return@mapNotNull null
            }
            val outcomes = outcomeByWagerIdMap[wagerItem.id]?.map { it.toDto() }
            if (outcomes == null) {
                logger.warn { "Outcomes not found for wager id=${wagerItem.id}" }
                return@mapNotNull null
            }
            FeedItem(
                id = wagerItem.id,
                name = wagerItem.name,
                text = wagerItem.text,
                createdAt = wagerItem.createdAt,
                media = media,
                outcomes = outcomes
            )
        }
        return items
    }
}