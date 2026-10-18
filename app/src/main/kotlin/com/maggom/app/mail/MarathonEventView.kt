package com.maggom.app.mail

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.domain.NoticePhase
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 메일 본문에 표시할 대회 정보.
 *
 * 저장 형식을 그대로 보여주면 곤란한 값(예: 하위 지역이 미확정인 `서울시 unknown`)을
 * 읽을 수 있는 형태로 다듬고, 접수 단계를 색이 다른 상태 표시로 함께 전달한다.
 */
data class MarathonEventView(
    val title: String,
    val eventDate: LocalDate,
    val region: String,
    val distances: List<String>,
    val regStartDate: LocalDateTime,
    val regEndDate: LocalDateTime?,
    val linkUrl: String,
    val status: StatusLabel,
) {
    /** 상태 표시 문구와 색 (메일 클라이언트 호환을 위해 인라인 스타일 값으로 넘긴다) */
    data class StatusLabel(
        val text: String,
        val textColor: String,
        val backgroundColor: String,
    )

    companion object {
        private const val UNKNOWN_REGION = "unknown"

        private val STATUS_LABELS = mapOf(
            NoticePhase.OPENING_SOON to StatusLabel("곧 접수 시작", "#1d4ed8", "#dbeafe"),
            NoticePhase.NEWLY_OPENED to StatusLabel("접수 시작", "#15803d", "#dcfce7"),
            NoticePhase.CLOSING_SOON to StatusLabel("마감 임박", "#b91c1c", "#fee2e2"),
            NoticePhase.UPCOMING to StatusLabel("접수 예정", "#4b5563", "#f3f4f6"),
            NoticePhase.OPEN to StatusLabel("접수중", "#4338ca", "#e0e7ff"),
        )

        fun from(event: MarathonEvent, now: LocalDateTime): MarathonEventView {
            return MarathonEventView(
                title = event.title,
                eventDate = event.eventDate,
                region = toDisplayRegion(event.region),
                distances = event.distances,
                regStartDate = event.regStartDate,
                regEndDate = event.regEndDate,
                linkUrl = event.linkUrl,
                status = STATUS_LABELS.getValue(NoticePhase.of(event, now)),
            )
        }

        /** 시도만 확인된 지역은 `서울시 unknown` 형태로 저장되므로 뒤쪽을 떼고 보여준다. */
        private fun toDisplayRegion(region: String): String {
            val trimmed = region.trim()
            if (trimmed.equals(UNKNOWN_REGION, ignoreCase = true)) return "지역 미정"

            return trimmed.removeSuffix(UNKNOWN_REGION).trim().ifEmpty { "지역 미정" }
        }
    }
}
