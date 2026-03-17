package com.maggom.app.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
@Profile("prod")
@EnableConfigurationProperties(SesMailProperties::class)
class SesMailConfig(
    private val sesMailProperties: SesMailProperties,
) {

    @Bean
    @Primary
    fun javaMailSender(): JavaMailSender {
        return JavaMailSenderImpl().apply {
            host = sesMailProperties.host
            port = sesMailProperties.port
            this.username = sesMailProperties.username
            this.password = sesMailProperties.password
            javaMailProperties.putAll(sesMailProperties.properties)
        }
    }
}
