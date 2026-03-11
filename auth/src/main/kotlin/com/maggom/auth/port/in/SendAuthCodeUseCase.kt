package com.maggom.auth.port.`in`

interface SendAuthCodeUseCase {
    fun sendAuthCode(command: SendAuthCodeCommand): SendAuthCodeResult
}

data class SendAuthCodeCommand(
    val email: String,
)

data class SendAuthCodeResult(
    val expiresInSeconds: Long,
)
