package com.maggom.event.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class NoticeOrderTest {

    private val now: LocalDateTime = LocalDateTime.of(2026, 10, 18, 8, 0)

    @Test
    @DisplayName("접수 단계 판정 - 발송 시점 날짜 기준")
    fun phase_is_decided_by_dates_at_send_time() {
        // when & then
        assertEquals(NoticePhase.OPENING_SOON, NoticePhase.of(event(start = now.plusDays(3)), now))
        assertEquals(NoticePhase.UPCOMING, NoticePhase.of(event(start = now.plusDays(20)), now))
        assertEquals(NoticePhase.NEWLY_OPENED, NoticePhase.of(event(start = now.minusDays(5), end = now.plusDays(30)), now))
        assertEquals(NoticePhase.CLOSING_SOON, NoticePhase.of(event(start = now.minusDays(30), end = now.plusDays(3)), now))
        assertEquals(NoticePhase.OPEN, NoticePhase.of(event(start = now.minusDays(30), end = now.plusDays(30)), now))
    }

    @Test
    @DisplayName("새로 열린 대회라도 7일 안에 마감되면 마감 임박")
    fun newly_opened_but_closing_soon_is_closing_soon() {
        // given
        val event = event(start = now.minusDays(3), end = now.plusDays(2))

        // when & then
        assertEquals(NoticePhase.CLOSING_SOON, NoticePhase.of(event, now))
    }

    @Test
    @DisplayName("시작일을 몰라 마감일과 같게 저장된 대회는 시작일로 판단하지 않는다")
    fun unknown_start_date_is_not_used_for_phase() {
        // given
        val deadline = now.plusDays(30)

        // when & then
        assertEquals(NoticePhase.OPEN, NoticePhase.of(event(start = deadline, end = deadline), now))
    }

    @Test
    @DisplayName("곧 접수 시작 > 새로 열림 > 마감 임박 > 접수 예정 > 오래 열린 접수중 순으로 정렬")
    fun sort_by_notice_phase() {
        // given
        val open = event(title = "오래 열림", start = now.minusDays(40), end = now.plusDays(30))
        val upcoming = event(title = "접수 예정", start = now.plusDays(20))
        val closing = event(title = "마감 임박", start = now.minusDays(30), end = now.plusDays(2))
        val newlyOpened = event(title = "새로 열림", start = now.minusDays(2), end = now.plusDays(40))
        val openingSoon = event(title = "곧 접수 시작", start = now.plusDays(2))

        // when
        val sorted = NoticeOrder.sort(listOf(open, upcoming, closing, newlyOpened, openingSoon), now)

        // then
        assertEquals(listOf("곧 접수 시작", "새로 열림", "마감 임박", "접수 예정", "오래 열림"), sorted.map { it.title })
    }

    @Test
    @DisplayName("단계 안에서는 오픈·마감이 빠른 순, 접수중은 최근에 연 순")
    fun sort_within_phase() {
        // given
        val openingLater = event(title = "오픈 5일 뒤", start = now.plusDays(5))
        val openingSooner = event(title = "오픈 1일 뒤", start = now.plusDays(1))
        val closingLater = event(title = "마감 6일 뒤", start = now.minusDays(30), end = now.plusDays(6))
        val closingSooner = event(title = "마감 1일 뒤", start = now.minusDays(30), end = now.plusDays(1))
        val openedLongAgo = event(title = "60일 전 오픈", start = now.minusDays(60), end = now.plusDays(30))
        val openedRecently = event(title = "20일 전 오픈", start = now.minusDays(20), end = now.plusDays(30))

        // when
        val sorted = NoticeOrder.sort(
            listOf(openingLater, openingSooner, closingLater, closingSooner, openedLongAgo, openedRecently),
            now,
        )

        // then
        assertEquals(
            listOf("오픈 1일 뒤", "오픈 5일 뒤", "마감 1일 뒤", "마감 6일 뒤", "20일 전 오픈", "60일 전 오픈"),
            sorted.map { it.title },
        )
    }

    @Test
    @DisplayName("우선 단계가 적으면 나머지로 20개까지 채운다")
    fun pick_fills_up_to_minimum_with_other_phases() {
        // given
        val events = List(5) { event(start = now.plusDays(1)) } + List(40) { event(start = now.plusDays(20)) }

        // when
        val picked = NoticeOrder.pick(NoticeOrder.sort(events, now), now)

        // then
        assertEquals(NoticeOrder.MIN_EVENTS, picked.size)
    }

    @Test
    @DisplayName("우선 단계가 20개를 넘으면 모두 보내되 30개까지만")
    fun pick_sends_all_priority_events_up_to_maximum() {
        // given
        val twentyFive = List(25) { event(start = now.plusDays(1)) } + List(10) { event(start = now.plusDays(20)) }
        val forty = List(40) { event(start = now.plusDays(1)) }

        // when & then
        assertEquals(25, NoticeOrder.pick(NoticeOrder.sort(twentyFive, now), now).size)
        assertEquals(NoticeOrder.MAX_EVENTS, NoticeOrder.pick(NoticeOrder.sort(forty, now), now).size)
    }

    @Test
    @DisplayName("대회 수가 적으면 있는 만큼만 보낸다")
    fun pick_sends_what_exists_when_few() {
        // given
        val events = List(3) { event(start = now.plusDays(20)) }

        // when & then
        assertEquals(3, NoticeOrder.pick(events, now).size)
    }

    private fun event(
        title: String = "테스트 마라톤",
        start: LocalDateTime,
        end: LocalDateTime? = null,
    ): MarathonEvent {
        return MarathonEvent(
            title = title,
            eventDate = LocalDate.of(2026, 12, 6),
            region = "서울시 마포구",
            distances = listOf("10K"),
            regStartDate = start,
            regEndDate = end,
            linkUrl = "https://example.com",
            status = if (start.isAfter(now)) MarathonEventStatus.UPCOMING else MarathonEventStatus.OPEN,
            sourceName = "test",
            sourceUrl = "https://source.com",
            crawledAtKst = now,
        )
    }
}
