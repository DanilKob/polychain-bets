# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean install

# Run application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ApplicationTests

# Build without tests
./mvnw clean package -DskipTests
```

## Architecture

Spring Boot 4 REST API written in Kotlin, backed by MongoDB.

**Stack:**
- Kotlin 2.2 / Java 21
- Spring Boot 4 with Spring MVC
- Spring Data MongoDB

**Package root:** `com.polychain.bets`

**Kotlin compiler settings:** `-Xjsr305=strict` (strict null-safety enforced), Spring all-open plugin enabled (required for Spring AOP proxying of Kotlin classes).

**Configuration:** `src/main/resources/application.properties` — MongoDB connection and server settings go here.

**Testing:** JUnit 5; test classes live under `src/test/kotlin/com/polychain/bets/`. Use `@SpringBootTest` for integration tests, `@WebMvcTest` for controller-layer tests.