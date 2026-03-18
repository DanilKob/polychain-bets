package com.polychain.bets.auth.controller

import com.polychain.bets.auth.entity.FirebasePrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@RestController
@RequestMapping("/debug")
class DebugController(
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/jwt-mirror")
    fun getJwtMirror(@AuthenticationPrincipal principal: FirebasePrincipal): ResponseEntity<JsonNode> {
        val json: JsonNode = objectMapper.valueToTree(principal)
        return ResponseEntity.ok(json)
    }
}