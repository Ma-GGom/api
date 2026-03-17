package com.maggom.app.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("maggom.mail.fallback")
data class FallbackMailProperties(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val fromEmail: String,
    val properties: Map<String, String> = emptyMap(),
)
