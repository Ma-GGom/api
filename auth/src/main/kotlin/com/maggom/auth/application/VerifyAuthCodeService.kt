package com.maggom.auth.application

import com.maggom.auth.port.`in`.AuthFlow
import com.maggom.auth.port.`in`.VerifyAuthCodeCommand
import com.maggom.auth.port.`in`.VerifyAuthCodeResult
import com.maggom.auth.port.`in`.VerifyAuthCodeUseCase
import com.maggom.auth.port.out.AuthCodeStoragePort
import com.maggom.auth.port.out.MemberCheckPort
import com.maggom.auth.port.out.MemberRegistrationPort
import com.maggom.auth.port.out.TokenPort
import com.maggom.auth.port.out.WelcomeMailPort
import org.springframework.stereotype.Service

@Service
class VerifyAuthCodeService(
    private val authCodeStoragePort: AuthCodeStoragePort,
    private val tokenPort: TokenPort,
    private val memberCheckPort: MemberCheckPort,
    private val memberRegistrationPort: MemberRegistrationPort,
    private val welcomeMailPort: WelcomeMailPort,
) : VerifyAuthCodeUseCase {

    override fun verifyAuthCode(command: VerifyAuthCodeCommand): VerifyAuthCodeResult {
        val entry = authCodeStoragePort.findByEmail(command.email)
            ?: throw IllegalArgumentException("인증 코드가 존재하지 않거나 만료되었습니다.")

        if (entry.code != command.code) {
            throw IllegalArgumentException("인증 코드가 올바르지 않습니다.")
        }

        authCodeStoragePort.delete(command.email)

        val role = if (entry.flow == AuthFlow.SUBSCRIBE) {
            memberRegistrationPort.register(command.email)
            welcomeMailPort.sendWelcomeMail(command.email)
            "MEMBER"
        } else {
            memberCheckPort.findRoleByEmail(command.email)
        }

        val token = tokenPort.generateToken(command.email, role)

        return VerifyAuthCodeResult(accessToken = token)
    }
}
