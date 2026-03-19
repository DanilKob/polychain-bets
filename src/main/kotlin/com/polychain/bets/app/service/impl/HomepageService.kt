package com.polychain.bets.app.service.impl

import com.polychain.bets.app.dto.HomepageInitResponse
import com.polychain.bets.app.dto.UserForInitResponse
import com.polychain.bets.app.service.HomepageServiceInterface
import com.polychain.bets.auth.service.UserService
import com.polychain.bets.config.CoroutineDispatcherHolder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class HomepageService(
    private val userService: UserService,
    private val coroutineDispatcherHolder: CoroutineDispatcherHolder
) : HomepageServiceInterface {

    override suspend fun init(
        userId: String
    ): HomepageInitResponse = coroutineScope {
        val userAsync = async(coroutineDispatcherHolder.dbDispatcher) {
            userService.findByUid(userId)
        }
        val user = userAsync.await() ?: throw RuntimeException("User not found")
        return@coroutineScope HomepageInitResponse(
            user = UserForInitResponse(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName
            ),
            feed = listOf()
        )
    }
}