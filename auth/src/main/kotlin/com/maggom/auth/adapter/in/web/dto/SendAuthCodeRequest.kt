package com.maggom.auth.adapter.`in`.web.dto

import com.maggom.auth.port.`in`.AuthFlow
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SendAuthCodeRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotNull(message = "flow는 필수입니다.")
    val flow: AuthFlow,
)
