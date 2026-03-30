package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.UserEntity

interface UserRepositoryInterface {
    suspend fun findById(id: String): UserEntity?
    suspend fun save(user: UserEntity): UserEntity
    suspend fun save(users: List<UserEntity>): List<UserEntity>
}