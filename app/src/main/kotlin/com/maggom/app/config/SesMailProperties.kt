package com.maggom.app.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.mail")
data class SesMailProperties(
    val host: String,
    val port: Int = 587,
    val username: String,
    val password: String,
    val properties: Map<String, String> = emptyMap(),
)
