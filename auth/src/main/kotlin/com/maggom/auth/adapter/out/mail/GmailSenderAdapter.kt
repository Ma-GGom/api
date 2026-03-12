package com.maggom.auth.adapter.out.mail

import com.maggom.auth.port.out.AuthCodeEmailMessage
import com.maggom.auth.port.out.EmailSenderPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class GmailSenderAdapter(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val fromEmail: String,
) : EmailSenderPort {

    override fun sendAuthCode(message: AuthCodeEmailMessage) {
        val mailMessage = SimpleMailMessage().apply {
            setFrom(fromEmail)
            setTo(message.to)
            subject = "[마꼼] 이메일 인증 번호"
            text = """
                ${message.code}

                위 인증 번호를 입력하여 인증을 완료해 주세요.
            """.trimIndent()
        }

        mailSender.send(mailMessage)
    }
}
