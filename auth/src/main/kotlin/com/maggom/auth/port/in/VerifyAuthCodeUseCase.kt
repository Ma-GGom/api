package com.maggom.auth.port.`in`

interface VerifyAuthCodeUseCase {
    fun verifyAuthCode(command: VerifyAuthCodeCommand): VerifyAuthCodeResult
}

data class VerifyAuthCodeCommand(
    val email: String,
    val code: String,
)

data class VerifyAuthCodeResult(
    val accessToken: String,
)
