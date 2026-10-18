package com.maggom.event.domain

import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * 알림 메일에 넣을 대회의 순서와 개수를 정한다.
 *
 * - 순서: 접수 단계(NoticePhase) 순, 같은 단계 안에서는 단계마다 정한 날짜 순, 마지막으로 대회일이 빠른 순
 * - 개수: 우선 단계(곧 접수 시작·새로 열림·마감 임박)는 모두 넣되 최대 [MAX_EVENTS]개,
 *   우선 단계가 적으면 나머지 단계로 [MIN_EVENTS]개까지 채운다.
 */
object NoticeOrder {
    const val MIN_EVENTS = 20
    const val MAX_EVENTS = 30

    fun sort(events: List<MarathonEvent>, now: LocalDateTime): List<MarathonEvent> {
        return events
            .map { it to NoticePhase.of(it, now) }
            .sortedWith(
                compareBy<Pair<MarathonEvent, NoticePhase>> { it.second.ordinal }
                    .thenBy { withinPhaseKey(it.first, it.second) }
                    .thenBy { it.first.eventDate }
            )
            .map { it.first }
    }

    /** 이미 [sort]한 목록에서 보낼 만큼만 앞에서 자른다. */
    fun pick(sortedEvents: List<MarathonEvent>, now: LocalDateTime): List<MarathonEvent> {
        val priorityCount = sortedEvents.count { NoticePhase.of(it, now).priority }

        return sortedEvents.take(priorityCount.coerceIn(MIN_EVENTS, MAX_EVENTS))
    }

    /**
     * 같은 단계 안의 정렬 키 (작을수록 앞).
     * 곧 접수 시작·접수 예정은 오픈이 빠른 순, 마감 임박은 마감이 빠른 순,
     * 접수중은 최근에 연 순(오래 열린 대회일수록 이미 찼을 가능성이 크다).
     */
    private fun withinPhaseKey(event: MarathonEvent, phase: NoticePhase): Long {
        return when (phase) {
            NoticePhase.OPENING_SOON, NoticePhase.UPCOMING -> epochSecond(event.regStartDate)
            NoticePhase.CLOSING_SOON -> epochSecond(event.regEndDate ?: event.regStartDate)
            NoticePhase.NEWLY_OPENED, NoticePhase.OPEN -> -epochSecond(event.regStartDate)
        }
    }

    private fun epochSecond(dateTime: LocalDateTime): Long = dateTime.toEpochSecond(ZoneOffset.UTC)
}
