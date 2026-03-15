package com.maggom.auth.port.`in`

interface VerifyAuthCodeUseCase {
    fun verifyAuthCode(command: VerifyAuthCodeCommand): VerifyAuthCodeResult
}
