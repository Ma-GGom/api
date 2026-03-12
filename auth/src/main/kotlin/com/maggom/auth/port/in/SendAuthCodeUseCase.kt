package com.maggom.auth.port.`in`

interface SendAuthCodeUseCase {
    fun sendAuthCode(command: SendAuthCodeCommand): SendAuthCodeResult
}
