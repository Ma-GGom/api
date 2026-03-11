package com.maggom.auth.adapter.`in`.web

import com.maggom.auth.adapter.`in`.web.dto.SendAuthCodeRequest
import com.maggom.auth.adapter.`in`.web.dto.SendAuthCodeResponse
import com.maggom.auth.adapter.`in`.web.dto.VerifyAuthCodeRequest
import com.maggom.auth.adapter.`in`.web.dto.VerifyAuthCodeResponse
import com.maggom.auth.port.`in`.SendAuthCodeCommand
import com.maggom.auth.port.`in`.SendAuthCodeUseCase
import com.maggom.auth.port.`in`.VerifyAuthCodeCommand
import com.maggom.auth.port.`in`.VerifyAuthCodeUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val sendAuthCodeUseCase: SendAuthCodeUseCase,
    private val verifyAuthCodeUseCase: VerifyAuthCodeUseCase,
) {

    @PostMapping("/email/send-code")
    @ResponseStatus(HttpStatus.OK)
    fun sendAuthCode(@Valid @RequestBody request: SendAuthCodeRequest): SendAuthCodeResponse {
        val result = sendAuthCodeUseCase.sendAuthCode(
            SendAuthCodeCommand(request.email)
        )

        return SendAuthCodeResponse(true, "발송 완료", result.expiresInSeconds)
    }

    @PostMapping("/email/verify")
    @ResponseStatus(HttpStatus.OK)
    fun verifyAuthCode(@Valid @RequestBody request: VerifyAuthCodeRequest): VerifyAuthCodeResponse {
        val result = verifyAuthCodeUseCase.verifyAuthCode(
            VerifyAuthCodeCommand(request.email, request.code)
        )

        return VerifyAuthCodeResponse(true, result.authToken)
    }
}
