package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.UserEntity
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class UserRepository(
    private val userMongoRepository: UserMongoRepository
) : UserRepositoryInterface {

    override suspend fun findById(id: String): UserEntity? =
        userMongoRepository.findByUid(id).awaitSingleOrNull()

    override suspend fun save(user: UserEntity): UserEntity =
        userMongoRepository.save(user).awaitSingle()

    override suspend fun save(users: List<UserEntity>): List<UserEntity> =
        userMongoRepository.saveAll(users).collectList().awaitSingle()
}