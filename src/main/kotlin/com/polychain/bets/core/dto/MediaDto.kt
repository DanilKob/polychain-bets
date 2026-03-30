package com.polychain.bets.core.dto

data class MediaDto(
    val id: String,
    val videoId: String,
    val videoToken: String,
    val originalFilename: String,
    val hlsUrl: String,
    val thumbnailUrl: String,
    val previewUrl: String,
    val qualities: List<String>,
    val durationSeconds: Int,
    val width: Int,
    val height: Int,
)