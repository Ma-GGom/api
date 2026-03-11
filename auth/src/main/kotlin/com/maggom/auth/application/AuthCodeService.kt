package com.maggom.auth.application

import com.maggom.auth.port.`in`.SendAuthCodeCommand
import com.maggom.auth.port.`in`.SendAuthCodeResult
import com.maggom.auth.port.`in`.SendAuthCodeUseCase
import com.maggom.auth.port.out.AuthCodeEmailMessage
import com.maggom.auth.port.out.AuthCodeEntry
import com.maggom.auth.port.out.AuthCodeStoragePort
import com.maggom.auth.port.out.EmailSenderPort
import com.maggom.common.exception.TooManyRequestsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Service
class AuthCodeService(
    private val authCodeStoragePort: AuthCodeStoragePort,
    private val emailSenderPort: EmailSenderPort,
    @Value("\${maggom.auth.code-expiry-minutes}") private val expiryMinutes: Long,
    @Value("\${maggom.auth.resend-cooldown-seconds}") private val cooldownSeconds: Long,
) : SendAuthCodeUseCase {

    override fun sendAuthCode(command: SendAuthCodeCommand): SendAuthCodeResult {
        val existing = authCodeStoragePort.findByEmail(command.email)
        if (existing != null) {
            val cooldownEndsAt = existing.sentAt.plusSeconds(cooldownSeconds)
            if (cooldownEndsAt.isAfter(LocalDateTime.now())) {
                val remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), cooldownEndsAt)
                throw TooManyRequestsException("${remaining}초 후에 재발송 가능합니다.")
            }
        }

        val authCode = generateAuthCode()
        val now = LocalDateTime.now()

        emailSenderPort.sendAuthCode(
            AuthCodeEmailMessage(command.email, authCode, expiryMinutes)
        )

        authCodeStoragePort.save(
            command.email,
            AuthCodeEntry(authCode, now.plusMinutes(expiryMinutes), now)
        )

        return SendAuthCodeResult(expiryMinutes * 60)
    }

    private fun generateAuthCode(): String {
        return String.format("%06d", Random.nextInt(0, 1000000))
    }
}
