package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.WagerStatsEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WagerStatsMongoRepository : ReactiveMongoRepository<WagerStatsEntity, String>
