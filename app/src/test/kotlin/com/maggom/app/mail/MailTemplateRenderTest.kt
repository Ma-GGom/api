package com.maggom.app.mail

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.domain.MarathonEventStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MailTemplateRenderTest {

    private val now: LocalDateTime = LocalDateTime.now()

    private val templateEngine = SpringTemplateEngine().apply {
        setTemplateResolver(
            ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                templateMode = TemplateMode.HTML
                characterEncoding = "UTF-8"
            }
        )
    }

    @Test
    @DisplayName("알림 메일 - 대회 수와 상태 표시가 카드마다 렌더링된다")
    fun notification_mail_renders_count_and_status_per_card() {
        // given
        val views = listOf(
            MarathonEventView.from(event(title = "곧 열리는 대회", start = now.plusDays(2)), now),
            MarathonEventView.from(event(title = "마감 임박 대회", start = now.minusDays(30), end = now.plusDays(2)), now),
        )

        // when
        val html = render("mail/notification", views)

        // then
        assertTrue(html.contains("<strong>2</strong>개를 알려드립니다"))
        assertTrue(html.contains("곧 접수 시작"))
        assertTrue(html.contains("마감 임박"))
        assertTrue(html.contains("background:#fee2e2"))
        assertEquals(2, Regex("max-width:272px").findAll(html).count())
    }

    @Test
    @DisplayName("환영 메일 - 같은 카드 조각을 쓴다")
    fun welcome_mail_uses_same_cards() {
        // given
        val views = listOf(MarathonEventView.from(event(title = "환영 예시 대회", start = now.plusDays(20)), now))

        // when
        val html = render("mail/welcome", views)

        // then
        assertTrue(html.contains("환영 예시 대회"))
        assertTrue(html.contains("접수 예정"))
    }

    private fun render(template: String, views: List<MarathonEventView>): String {
        val context = Context(Locale.KOREAN).apply {
            setVariable("events", views)
            setVariable("unsubscribeUrl", "https://maggom.com/unsubscribe")
        }

        return templateEngine.process(template, context)
    }

    private fun event(title: String, start: LocalDateTime, end: LocalDateTime? = null) = MarathonEvent(
        title = title,
        eventDate = LocalDate.now().plusMonths(2),
        region = "서울시 unknown",
        distances = listOf("10K", "HALF"),
        regStartDate = start,
        regEndDate = end,
        linkUrl = "https://example.com",
        status = if (start.isAfter(now)) MarathonEventStatus.UPCOMING else MarathonEventStatus.OPEN,
        sourceName = "test",
        sourceUrl = "https://source.com",
        crawledAtKst = now,
    )
}
