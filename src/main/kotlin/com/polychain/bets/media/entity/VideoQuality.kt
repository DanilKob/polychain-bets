package com.polychain.bets.media.entity

enum class VideoQuality(val value: String) {
    QUALITY_360("360p"),
    QUALITY_720("720p"),
    QUALITY_1080("1080p"),
    QUALITY_2K("2K"),
    QUALITY_4K("4K"),
    QUALITY_8K("8K");

    companion object {
        fun fromValue(value: String): VideoQuality? {
            return entries.find { it.value == value }
        }
    }
}