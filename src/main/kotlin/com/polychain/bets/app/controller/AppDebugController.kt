package com.polychain.bets.app.controller

import com.polychain.bets.app.dto.CreateWagerRequest
import com.polychain.bets.app.dto.HomepageResponse
import com.polychain.bets.app.service.HomepageServiceInterface
import com.polychain.bets.core.service.WagerServiceInterface
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/debug")
class AppDebugController(
    private val homepageService: HomepageServiceInterface,
    private val wagerService: WagerServiceInterface
) {
    @RequestMapping("/homepage/{userId}")
    fun getDebugHomepage(@PathVariable userId: String): HomepageResponse = runBlocking {
        return@runBlocking homepageService.getHomepage(userId)
    }

    @PostMapping("/wager")
    fun postDebugHomepage(
        @RequestBody request: CreateWagerRequest
    ): Unit = runBlocking {
        return@runBlocking wagerService.createDebugWagerForController(
            request = request
        )
    }

    @PostMapping("/wagers")
    fun postDebugHomepage(): Unit = runBlocking {
        return@runBlocking wagerService.createWagersForController(
            n = 100
        )
    }
}