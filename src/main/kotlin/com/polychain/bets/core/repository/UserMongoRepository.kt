package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.UserEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserMongoRepository : ReactiveMongoRepository<UserEntity, String> {
    suspend fun findByUid(id: String): UserEntity?
}