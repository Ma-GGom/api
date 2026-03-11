package com.maggom.auth.application

import com.maggom.auth.port.`in`.VerifyAuthCodeCommand
import com.maggom.auth.port.`in`.VerifyAuthCodeResult
import com.maggom.auth.port.`in`.VerifyAuthCodeUseCase
import com.maggom.auth.port.out.AuthCodeStoragePort
import com.maggom.auth.port.out.AuthTokenStoragePort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VerifyAuthCodeService(
    private val authCodeStoragePort: AuthCodeStoragePort,
    private val authTokenStoragePort: AuthTokenStoragePort,
) : VerifyAuthCodeUseCase {

    override fun verifyAuthCode(command: VerifyAuthCodeCommand): VerifyAuthCodeResult {
        val entry = authCodeStoragePort.findByEmail(command.email)
            ?: throw IllegalArgumentException("인증 코드가 존재하지 않거나 만료되었습니다.")

        if (entry.code != command.code) {
            throw IllegalArgumentException("인증 코드가 올바르지 않습니다.")
        }

        authCodeStoragePort.delete(command.email)

        val token = UUID.randomUUID().toString()
        authTokenStoragePort.save(token, command.email)

        return VerifyAuthCodeResult(token)
    }
}
