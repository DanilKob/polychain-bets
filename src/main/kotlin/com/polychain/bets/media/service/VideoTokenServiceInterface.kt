package com.polychain.bets.media.service

import com.polychain.bets.media.dto.VideoToken

interface VideoTokenServiceInterface {
    fun issueToken(videoId: String): VideoToken
    fun validateToken(videoId: String, token: String)
}