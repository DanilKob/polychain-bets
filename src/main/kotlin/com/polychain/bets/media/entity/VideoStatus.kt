package com.polychain.bets.media.entity

enum class VideoStatus {
    PENDING,      // just uploaded, waiting for processing
    PROCESSING,   // currently being transcoded
    PUBLISHED,    // processing done, available
    FAILED        // processing failed
}