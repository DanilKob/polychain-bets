package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.WagerEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WagerMongoRepository : ReactiveMongoRepository<WagerEntity, String>