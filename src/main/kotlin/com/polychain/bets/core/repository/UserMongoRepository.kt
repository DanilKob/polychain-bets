package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.UserEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserMongoRepository : ReactiveMongoRepository<UserEntity, String> {
    fun findByUid(id: String): Mono<UserEntity>
}