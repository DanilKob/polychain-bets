package com.polychain.bets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration

@SpringBootApplication(
	// disable Spring Boot's autoconfiguration of the UserDetailsService' for basic authentication
	exclude = [UserDetailsServiceAutoConfiguration::class]
)
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
