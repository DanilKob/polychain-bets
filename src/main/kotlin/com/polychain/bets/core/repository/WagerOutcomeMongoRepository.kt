package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.WagerOutcomeEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface WagerOutcomeMongoRepository : ReactiveMongoRepository<WagerOutcomeEntity, String> {
    fun findByWagerIdInAndDeletedFalse(wagerIds: List<String>): Flux<WagerOutcomeEntity>
}