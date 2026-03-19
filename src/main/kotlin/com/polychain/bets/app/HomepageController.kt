package com.polychain.bets.app

import com.polychain.bets.app.dto.HomepageInitResponse
import com.polychain.bets.app.service.HomepageServiceInterface
import com.polychain.bets.auth.entity.FirebasePrincipal
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/homepage")
class HomepageController(
    private val homepageService: HomepageServiceInterface
) {
    @PostMapping("/init")
    fun init(
        @AuthenticationPrincipal principal: FirebasePrincipal
    ): HomepageInitResponse = runBlocking {
        return@runBlocking homepageService.init(principal.uid)
    }
}