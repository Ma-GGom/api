package com.maggom.auth.adapter.`in`.web.dto

data class SendAuthCodeResponse(
    val message: String,
    val expiresIn: Long,
)
