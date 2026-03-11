package com.maggom.auth.adapter.out.mail

import com.maggom.auth.port.out.AuthCodeEmailMessage
import com.maggom.auth.port.out.EmailSenderPort
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class GmailSenderAdapter(
    private val mailSender: JavaMailSender,
) : EmailSenderPort {

    override fun sendAuthCode(message: AuthCodeEmailMessage) {
        val mailMessage = SimpleMailMessage().apply {
            setTo(message.to)
            subject = "[마곰] 이메일 인증 코드"
            text = """
                안녕하세요, 마곰입니다.

                인증 코드: ${message.code}

                위 코드를 입력하여 인증을 완료해주세요.
                인증 코드는 ${message.expiryMinutes}분간 유효합니다.
            """.trimIndent()
        }

        mailSender.send(mailMessage)
    }
}
