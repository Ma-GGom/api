package com.maggom.auth.port.out

interface EmailSenderPort {
    fun sendAuthCode(message: AuthCodeEmailMessage)
}

data class AuthCodeEmailMessage(
    val to: String,
    val code: String,
    val expiryMinutes: Long,
)
