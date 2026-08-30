package com.maggom.app.adapter

import com.maggom.app.config.FallbackMailProperties
import com.maggom.auth.port.out.AuthCodeEmailMessage
import com.maggom.auth.port.out.EmailSenderPort
import com.maggom.auth.port.out.TestMailPort
import com.maggom.auth.port.out.WelcomeMailPort
import com.maggom.event.domain.MarathonEvent
import com.maggom.event.port.out.MarathonEventPort
import com.maggom.event.port.out.NotificationMailPort
import com.maggom.member.port.out.UnsubscribeTokenPort
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.Locale

@Component
class MailAdapter(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    @Value("\${spring.mail.from-email}") private val fromEmail: String,
    @Value("\${spring.mail.from-name}") private val fromName: String,
    @Value("\${maggom.mail.base-url}") private val baseUrl: String,
    @Autowired(required = false) private val fallbackMailProps: FallbackMailProperties?,
    @Autowired(required = false) @Qualifier("gmailMailSender") private val fallbackMailSender: JavaMailSender?,
    private val marathonEventPort: MarathonEventPort,
    private val unsubscribeTokenPort: UnsubscribeTokenPort,
) : EmailSenderPort, WelcomeMailPort, NotificationMailPort, TestMailPort {

    companion object {
        private val DEFAULT_REGIONS = listOf("수도권")
        private const val WELCOME_EVENTS_COUNT = 2
        private const val UNSUBSCRIBE_PATH = "/api/v1/subscriptions/unsubscribe"
    }

    private val log = LoggerFactory.getLogger(javaClass)

    private data class MailSpec(
        val to: String,
        val subject: String,
        val template: String,
        val context: Context,
        val unsubscribeUrl: String? = null,
    )

    private data class MailPayload(
        val to: String,
        val subject: String,
        val html: String,
        val unsubscribeUrl: String?,
    )

    override fun sendAuthCode(message: AuthCodeEmailMessage) {
        val context = Context(Locale.KOREAN).apply {
            setVariable("code", message.code)
            setVariable("expiryMinutes", message.expiryMinutes)
        }
        sendHtml(
            MailSpec(
                to = message.to,
                subject = "[마꼼] 이메일 인증 번호",
                template = "mail/auth-code",
                context = context,
            )
        )
    }

    override fun sendWelcomeMail(to: String) {
        val events = marathonEventPort.findOpenByRegions(DEFAULT_REGIONS).take(WELCOME_EVENTS_COUNT)
        val unsubscribeUrl = unsubscribeUrl(to)
        val context = Context(Locale.KOREAN).apply {
            setVariable("events", events)
            setVariable("unsubscribeUrl", unsubscribeUrl)
        }
        sendHtml(
            MailSpec(
                to = to,
                subject = "[마꼼] 구독을 시작했어요! 🎉",
                template = "mail/welcome",
                context = context,
                unsubscribeUrl = unsubscribeUrl,
            )
        )
    }

    override fun sendNotification(to: String, events: List<MarathonEvent>) {
        val unsubscribeUrl = unsubscribeUrl(to)
        val context = Context(Locale.KOREAN).apply {
            setVariable("events", events)
            setVariable("unsubscribeUrl", unsubscribeUrl)
        }
        sendHtml(
            MailSpec(
                to = to,
                subject = "[마꼼] 마라톤 대회 목록 🏃",
                template = "mail/notification",
                context = context,
                unsubscribeUrl = unsubscribeUrl,
            )
        )
    }

    override fun sendTestMail(to: String, templateType: String) {
        val context = Context(Locale.KOREAN).apply {
            setVariable("templateType", templateType)
        }
        sendHtml(
            MailSpec(
                to = to,
                subject = "[마꼼] 테스트 메일 발송",
                template = "mail/test",
                context = context,
            )
        )
    }

    private fun unsubscribeUrl(to: String): String {
        return "$baseUrl$UNSUBSCRIBE_PATH?token=${unsubscribeTokenPort.generate(to)}"
    }

    private fun sendHtml(spec: MailSpec) {
        val payload = MailPayload(
            to = spec.to,
            subject = spec.subject,
            html = templateEngine.process(spec.template, spec.context),
            unsubscribeUrl = spec.unsubscribeUrl,
        )
        try {
            doSend(mailSender, fromEmail, payload)
        } catch (e: MailException) {
            if (fallbackMailSender != null && fallbackMailProps != null) {
                log.warn("주 발송 실패, Gmail 폴백 시도 [to={}, subject={}]: {}", spec.to, spec.subject, e.message)
                doSend(fallbackMailSender, fallbackMailProps.fromEmail, payload)
            } else {
                throw e
            }
        }
    }

    private fun doSend(sender: JavaMailSender, from: String, payload: MailPayload) {
        val message: MimeMessage = sender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(InternetAddress(from, fromName, "UTF-8"))
        helper.setTo(payload.to)
        helper.setSubject(payload.subject)
        helper.setText(payload.html, true)

        // RFC 8058 원클릭 수신 거부 - 메일 클라이언트가 구독 취소 버튼을 노출한다.
        payload.unsubscribeUrl?.let {
            message.addHeader("List-Unsubscribe", "<$it>")
            message.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click")
        }

        sender.send(message)
    }
}
