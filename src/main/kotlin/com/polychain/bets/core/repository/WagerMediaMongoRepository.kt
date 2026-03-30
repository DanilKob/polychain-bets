package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.WagerMediaEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WagerMediaMongoRepository : ReactiveMongoRepository<WagerMediaEntity, String> {

    fun findByVideoVideoId(videoId: String): Mono<WagerMediaEntity>

    fun countByVideoVideoId(videoId: String): Mono<Long>
}