package com.maggom.auth.adapter.`in`.web.dto

data class SendAuthCodeResponse(
    val success: Boolean,
    val message: String,
    val expiresIn: Long,
)
