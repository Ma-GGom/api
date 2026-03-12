package com.maggom.auth.port.`in`

data class VerifyAuthCodeCommand(
    val email: String,
    val code: String,
)
