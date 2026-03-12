package com.maggom.auth.port.`in`

interface SendAuthCodeUseCase {
    fun sendAuthCode(command: SendAuthCodeCommand): SendAuthCodeResult
}

enum class AuthFlow {
    SUBSCRIBE,
    SETTINGS,
}

data class SendAuthCodeCommand(
    val email: String,
    val flow: AuthFlow,
)

data class SendAuthCodeResult(
    val expiresInSeconds: Long,
)
