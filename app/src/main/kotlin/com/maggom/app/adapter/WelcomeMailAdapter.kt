package com.maggom.app.adapter

import com.maggom.auth.port.out.WelcomeMailPort
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class WelcomeMailAdapter(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val fromEmail: String,
) : WelcomeMailPort {

    override fun sendWelcomeMail(to: String) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(to)
        helper.setSubject("[마곰] 구독을 시작했어요! 이런 정보를 보내드려요 🏃")
        helper.setText(buildHtmlBody(), true)

        mailSender.send(message)
    }

    private fun buildHtmlBody(): String {
        val sampleEventRows = """
            <div style="border:1px solid #e0e0e0; border-radius:8px; padding:16px; margin-bottom:16px;">
                <h3 style="margin:0 0 8px 0; color:#333;">2025 서울 봄 마라톤</h3>
                <p style="margin:4px 0; color:#666;">📅 대회일: 2025.04.20</p>
                <p style="margin:4px 0; color:#666;">📍 지역: 서울</p>
                <p style="margin:4px 0; color:#666;">🏅 코스: 10K, 하프, 풀</p>
                <p style="margin:4px 0; color:#666;">📋 접수: 2025.03.01 09:00 ~ 2025.04.01 23:59</p>
                <span style="display:inline-block; margin-top:8px; padding:8px 16px; background:#ccc; color:white; border-radius:4px;">신청하기</span>
            </div>
            <div style="border:1px solid #e0e0e0; border-radius:8px; padding:16px; margin-bottom:16px;">
                <h3 style="margin:0 0 8px 0; color:#333;">2025 한강 하프마라톤</h3>
                <p style="margin:4px 0; color:#666;">📅 대회일: 2025.05.11</p>
                <p style="margin:4px 0; color:#666;">📍 지역: 경기</p>
                <p style="margin:4px 0; color:#666;">🏅 코스: 하프</p>
                <p style="margin:4px 0; color:#666;">📋 접수: 2025.03.15 10:00 ~ 2025.04.20 18:00</p>
                <span style="display:inline-block; margin-top:8px; padding:8px 16px; background:#ccc; color:white; border-radius:4px;">신청하기</span>
            </div>
        """.trimIndent()

        return """
            <div style="font-family: sans-serif; max-width:600px; margin:0 auto; padding:20px;">
                <h2 style="color:#333; border-bottom:2px solid #4CAF50; padding-bottom:8px;">🎉 마곰 구독을 시작했어요!</h2>
                <p style="color:#666;">설정하신 조건에 맞는 마라톤 대회 정보를 정기적으로 이메일로 보내드려요.</p>
                <p style="color:#666; margin-bottom:20px;">아래는 알림이 오면 이런 형식으로 받아보실 수 있어요 (샘플입니다).</p>
                <h3 style="color:#333; border-bottom:1px solid #e0e0e0; padding-bottom:8px;">🏃 마라톤 대회 알림</h3>
                $sampleEventRows
                <hr style="border:none; border-top:1px solid #e0e0e0; margin:20px 0;">
                <p style="color:#999; font-size:12px;">수신 설정 변경 또는 구독 해지는 마곰 서비스에서 가능합니다.</p>
            </div>
        """.trimIndent()
    }
}
