package com.maggom.auth.adapter.`in`.web.dto

data class VerifyAuthCodeResponse(
    val success: Boolean,
    val authToken: String,
)
