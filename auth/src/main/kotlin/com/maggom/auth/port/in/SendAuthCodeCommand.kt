package com.maggom.auth.port.`in`

data class SendAuthCodeCommand(
    val email: String,
    val flow: AuthFlow,
)
