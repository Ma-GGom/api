package com.maggom.app.notification

import com.maggom.event.domain.MarathonEvent
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class NotificationMailAdapter(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val fromEmail: String,
) : NotificationMailPort {

    override fun sendNotification(to: String, events: List<MarathonEvent>) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(to)
        helper.setSubject("[마곰] 이번 주 마라톤 대회 알림")
        helper.setText(buildHtmlBody(events), true)

        mailSender.send(message)
    }

    private fun buildHtmlBody(events: List<MarathonEvent>): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

        val eventRows = events.joinToString("") { event ->
            val regEnd = event.regEndDate?.format(dateTimeFormatter) ?: "미정"
            """
            <div style="border:1px solid #e0e0e0; border-radius:8px; padding:16px; margin-bottom:16px;">
                <h3 style="margin:0 0 8px 0; color:#333;">${event.title}</h3>
                <p style="margin:4px 0; color:#666;">📅 대회일: ${event.eventDate.format(dateFormatter)}</p>
                <p style="margin:4px 0; color:#666;">📍 지역: ${event.region}</p>
                <p style="margin:4px 0; color:#666;">🏅 코스: ${event.distances.joinToString(", ")}</p>
                <p style="margin:4px 0; color:#666;">📋 접수: ${event.regStartDate.format(dateTimeFormatter)} ~ $regEnd</p>
                <a href="${event.linkUrl}" style="display:inline-block; margin-top:8px; padding:8px 16px; background:#4CAF50; color:white; text-decoration:none; border-radius:4px;">신청하기</a>
            </div>
            """.trimIndent()
        }

        return """
            <div style="font-family: sans-serif; max-width:600px; margin:0 auto; padding:20px;">
                <h2 style="color:#333; border-bottom:2px solid #4CAF50; padding-bottom:8px;">🏃 마라톤 대회 알림</h2>
                <p style="color:#666;">안녕하세요! 구독하신 조건에 맞는 대회 정보를 알려드립니다.</p>
                $eventRows
                <hr style="border:none; border-top:1px solid #e0e0e0; margin:20px 0;">
                <p style="color:#999; font-size:12px;">수신 설정 변경 또는 구독 해지는 마곰 서비스에서 가능합니다.</p>
            </div>
        """.trimIndent()
    }
}
