package com.polychain.bets.config

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class CoroutineDispatcherHolder {

    lateinit var dbDispatcher: CoroutineDispatcher

    lateinit var telegramBotDispatcher: CoroutineDispatcher

    @PostConstruct
    fun init() {
        dbDispatcher = Dispatchers.IO
        telegramBotDispatcher = Dispatchers.Default
    }
}