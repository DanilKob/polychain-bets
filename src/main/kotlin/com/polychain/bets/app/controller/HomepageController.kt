package com.polychain.bets.app.controller

import com.polychain.bets.app.dto.HomepageResponse
import com.polychain.bets.app.service.HomepageServiceInterface
import com.polychain.bets.auth.entity.FirebasePrincipal
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/homepage")
class HomepageController(
    private val homepageService: HomepageServiceInterface
) {
    @GetMapping
    fun getHomepage(
        @AuthenticationPrincipal principal: FirebasePrincipal
    ): HomepageResponse = runBlocking {
        return@runBlocking homepageService.getHomepage(principal.uid)
    }
}