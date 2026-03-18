package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByUid(uid: String): User?
}