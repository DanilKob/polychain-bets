package com.polychain.bets.core.repository

import com.polychain.bets.core.entity.WagerOutcomeVoteEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WagerOutcomeVoteMongoRepository : ReactiveMongoRepository<WagerOutcomeVoteEntity, String>