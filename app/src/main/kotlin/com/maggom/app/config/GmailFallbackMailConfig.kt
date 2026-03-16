package com.maggom.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
@Profile("prod")
class GmailFallbackMailConfig(
    @Value("\${maggom.mail.fallback.host}") private val host: String,
    @Value("\${maggom.mail.fallback.port}") private val port: Int,
    @Value("\${maggom.mail.fallback.username}") private val username: String,
    @Value("\${maggom.mail.fallback.password}") private val password: String,
) {

    @Bean("gmailMailSender")
    fun gmailMailSender(): JavaMailSender {
        return JavaMailSenderImpl().apply {
            host = this@GmailFallbackMailConfig.host
            port = this@GmailFallbackMailConfig.port
            this.username = this@GmailFallbackMailConfig.username
            this.password = this@GmailFallbackMailConfig.password
            javaMailProperties.apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
                put("mail.smtp.ssl.trust", "smtp.gmail.com")
                put("mail.smtp.connectiontimeout", "5000")
                put("mail.smtp.timeout", "5000")
                put("mail.smtp.writetimeout", "5000")
            }
        }
    }
}
