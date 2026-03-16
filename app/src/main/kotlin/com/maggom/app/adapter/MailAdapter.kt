package com.maggom.app.adapter

import com.maggom.auth.port.out.AuthCodeEmailMessage
import com.maggom.auth.port.out.EmailSenderPort
import com.maggom.auth.port.out.TestMailPort
import com.maggom.auth.port.out.WelcomeMailPort
import com.maggom.event.domain.MarathonEvent
import com.maggom.event.port.out.MarathonEventPort
import com.maggom.event.port.out.NotificationMailPort
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
    @Value("\${maggom.mail.fallback.from-email:#{null}}") private val fallbackFromEmail: String?,
    @Autowired(required = false) @Qualifier("gmailMailSender") private val fallbackMailSender: JavaMailSender?,
    private val marathonEventPort: MarathonEventPort,
) : EmailSenderPort, WelcomeMailPort, NotificationMailPort, TestMailPort {

    companion object {
        private val DEFAULT_REGIONS = listOf("수도권")
        private const val WELCOME_EVENTS_COUNT = 2
    }

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendAuthCode(message: AuthCodeEmailMessage) {
        val context = Context(Locale.KOREAN).apply {
            setVariable("code", message.code)
            setVariable("expiryMinutes", message.expiryMinutes)
        }
        sendHtml(
            to = message.to,
            subject = "[마꼼] 이메일 인증 번호",
            template = "mail/auth-code",
            context = context,
        )
    }

    override fun sendWelcomeMail(to: String) {
        val events = marathonEventPort.findOpenByRegions(DEFAULT_REGIONS).take(WELCOME_EVENTS_COUNT)
        val context = Context(Locale.KOREAN).apply {
            setVariable("events", events)
        }
        sendHtml(
            to = to,
            subject = "[마꼼] 구독을 시작했어요! 🏃",
            template = "mail/welcome",
            context = context,
        )
    }

    override fun sendNotification(to: String, events: List<MarathonEvent>) {
        val context = Context(Locale.KOREAN).apply {
            setVariable("events", events)
        }
        sendHtml(
            to = to,
            subject = "[마꼼] 이번 주 마라톤 대회 알림",
            template = "mail/notification",
            context = context,
        )
    }

    override fun sendTestMail(to: String, templateType: String) {
        val context = Context(Locale.KOREAN).apply {
            setVariable("templateType", templateType)
        }
        sendHtml(
            to = to,
            subject = "[마꼼] 테스트 메일 발송",
            template = "mail/test",
            context = context,
        )
    }

    private fun sendHtml(to: String, subject: String, template: String, context: Context) {
        val html = templateEngine.process(template, context)
        try {
            doSend(mailSender, fromEmail, to, subject, html)
        } catch (e: MailException) {
            if (fallbackMailSender != null && fallbackFromEmail != null) {
                log.warn("주 발송 실패, Gmail 폴백 시도 [to={}, subject={}]: {}", to, subject, e.message)
                doSend(fallbackMailSender, fallbackFromEmail, to, subject, html)
            } else {
                throw e
            }
        }
    }

    private fun doSend(sender: JavaMailSender, from: String, to: String, subject: String, html: String) {
        val message: MimeMessage = sender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(InternetAddress(from, fromName, "UTF-8"))
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(html, true)

        sender.send(message)
    }
}
