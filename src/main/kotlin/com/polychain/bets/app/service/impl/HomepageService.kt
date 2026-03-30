package com.polychain.bets.app.service.impl

import com.polychain.bets.app.dto.HomepageResponse
import com.polychain.bets.app.dto.UserResponse
import com.polychain.bets.app.service.HomepageServiceInterface
import com.polychain.bets.auth.service.UserService
import com.polychain.bets.core.service.DEFAULT_PAGE_SIZE
import com.polychain.bets.core.service.FeedService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class HomepageService(
    private val userService: UserService,
    private val feedService: FeedService
) : HomepageServiceInterface {

    override suspend fun getHomepage(userId: String): HomepageResponse = coroutineScope {
        val userAsync = async {
            userService.findByUid(userId) ?: throw RuntimeException("User not found")
        }
        val feedAsync = async {
            feedService.getFeed(
                tags = null,
                encodedCursor = null,
                limit = DEFAULT_PAGE_SIZE
            )
        }
        val user = userAsync.await()
        val feed = feedAsync.await()
        return@coroutineScope HomepageResponse(
            user = UserResponse(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName
            ),
            feed = feed
        )
    }
}