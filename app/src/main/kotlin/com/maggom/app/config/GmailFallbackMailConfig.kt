package com.maggom.app.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
@Profile("prod")
@EnableConfigurationProperties(FallbackMailProperties::class)
class GmailFallbackMailConfig(
    private val props: FallbackMailProperties,
) {

    @Bean("gmailMailSender")
    fun gmailMailSender(): JavaMailSender {
        return JavaMailSenderImpl().apply {
            host = props.host
            port = props.port
            this.username = props.username
            this.password = props.password
            javaMailProperties.putAll(props.properties)
        }
    }
}
