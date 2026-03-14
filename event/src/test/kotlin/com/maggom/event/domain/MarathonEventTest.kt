package com.maggom.event.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class MarathonEventTest {

    private val now = LocalDateTime.now()

    @Test
    fun `유효한 값으로 생성 성공`() {
        assertDoesNotThrow {
            event()
        }
    }

    @Test
    fun `title이 빈 문자열이면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            event(title = "")
        }

        assertEquals("대회명은 필수입니다.", ex.message)
    }

    @Test
    fun `region이 빈 문자열이면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            event(region = "")
        }

        assertEquals("지역은 필수입니다.", ex.message)
    }

    @Test
    fun `distances가 비어있으면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            event(distances = emptyList())
        }

        assertEquals("코스는 하나 이상 있어야 합니다.", ex.message)
    }

    @Test
    fun `linkUrl이 http로 시작하면 성공`() {
        assertDoesNotThrow {
            event(linkUrl = "http://example.com")
        }
    }

    @Test
    fun `linkUrl이 https로 시작하면 성공`() {
        assertDoesNotThrow {
            event(linkUrl = "https://example.com")
        }
    }

    @Test
    fun `linkUrl이 http나 https로 시작하지 않으면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            event(linkUrl = "ftp://example.com")
        }

        assertEquals("링크 URL은 http/https로 시작해야 합니다.", ex.message)
    }

    @Test
    fun `regEndDate가 regStartDate보다 이전이면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            event(
                regStartDate = now.plusDays(5),
                regEndDate = now.plusDays(1),
            )
        }

        assertEquals("접수 종료일은 시작일보다 빠를 수 없습니다.", ex.message)
    }

    @Test
    fun `regEndDate가 regStartDate와 같으면 성공`() {
        assertDoesNotThrow {
            event(
                regStartDate = now,
                regEndDate = now,
            )
        }
    }

    @Test
    fun `regEndDate가 null이면 성공`() {
        assertDoesNotThrow {
            event(regEndDate = null)
        }
    }

    private fun event(
        title: String = "서울 마라톤",
        region: String = "수도권",
        distances: List<String> = listOf("10K", "HALF"),
        linkUrl: String = "https://example.com",
        regStartDate: LocalDateTime = now.minusDays(1),
        regEndDate: LocalDateTime? = now.plusDays(7),
    ) = MarathonEvent(
        title = title,
        eventDate = LocalDate.now().plusMonths(1),
        region = region,
        distances = distances,
        regStartDate = regStartDate,
        regEndDate = regEndDate,
        linkUrl = linkUrl,
        status = MarathonEventStatus.OPEN,
        sourceName = "test",
        sourceUrl = "https://source.com",
        crawledAtKst = now,
    )
}
