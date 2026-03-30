package com.polychain.bets.media.entity

enum class TranscodingQuality(
    val resolution: String,
    val videoBitrate: Int,   // kbps
    val scale: String        // ffmpeg scale filter — width fixed, height auto-calculated
                             // to preserve source aspect ratio (-2 rounds to even number)
) {
    Q360P("360p", 400, "640:-2"),
    Q720P("720p", 1500, "1280:-2"),
    Q1080P("1080p", 4000, "1920:-2")
}