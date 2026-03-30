package com.polychain.bets.media.repository

import com.polychain.bets.media.entity.VideoEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VideoRepository : ReactiveMongoRepository<VideoEntity, UUID>