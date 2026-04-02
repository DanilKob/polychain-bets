package com.polychain.bets.app.controller

import com.polychain.bets.core.dto.WagerStatsDto
import com.polychain.bets.core.service.WagerStatsServiceInterface
import com.polychain.bets.exception.NotFoundException
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/wager")
class WagerController(
    private val wagerStatsService: WagerStatsServiceInterface,
) {

    @GetMapping("/{id}/stats")
    fun getWagerStats(@PathVariable id: String): WagerStatsDto = runBlocking {
        wagerStatsService.getStats(id)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
}