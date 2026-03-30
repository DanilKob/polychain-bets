package com.polychain.bets.exception

import java.util.UUID

class NotFoundException(message: String) : RuntimeException(message)

class VideoNotFoundException(id: UUID) : RuntimeException("Video not found: $id")

class FfmpegProcessingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class StorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class VideoTokenInvalidException(message: String) : RuntimeException(message)