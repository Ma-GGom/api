package com.maggom.app.mail

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.domain.MarathonEventStatus
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 메일 본문에 표시할 대회 정보.
 *
 * 저장 형식을 그대로 보여주면 곤란한 값(예: 하위 지역이 미확정인 `서울시 unknown`)을
 * 읽을 수 있는 형태로 다듬고, 접수 임박 여부를 함께 전달한다.
 */
data class MarathonEventView(
    val title: String,
    val eventDate: LocalDate,
    val region: String,
    val distances: List<String>,
    val regStartDate: LocalDateTime,
    val regEndDate: LocalDateTime?,
    val linkUrl: String,
    val badge: String?,
) {
    companion object {
        private const val UNKNOWN_REGION = "unknown"
        private const val IMMINENT_DAYS = 7L
        private const val BADGE_OPENING_SOON = "곧 접수 시작"
        private const val BADGE_CLOSING_SOON = "접수 마감 임박"

        fun from(event: MarathonEvent, now: LocalDateTime): MarathonEventView {
            return MarathonEventView(
                title = event.title,
                eventDate = event.eventDate,
                region = toDisplayRegion(event.region),
                distances = event.distances,
                regStartDate = event.regStartDate,
                regEndDate = event.regEndDate,
                linkUrl = event.linkUrl,
                badge = toBadge(event, now),
            )
        }

        /** 시도만 확인된 지역은 `서울시 unknown` 형태로 저장되므로 뒤쪽을 떼고 보여준다. */
        private fun toDisplayRegion(region: String): String {
            val trimmed = region.trim()
            if (trimmed.equals(UNKNOWN_REGION, ignoreCase = true)) return "지역 미정"

            return trimmed.removeSuffix(UNKNOWN_REGION).trim().ifEmpty { "지역 미정" }
        }

        private fun toBadge(event: MarathonEvent, now: LocalDateTime): String? {
            if (event.status == MarathonEventStatus.UPCOMING) {
                return if (isWithinImminentDays(now, event.regStartDate)) BADGE_OPENING_SOON else null
            }

            val regEndDate = event.regEndDate ?: return null

            return if (isWithinImminentDays(now, regEndDate)) BADGE_CLOSING_SOON else null
        }

        private fun isWithinImminentDays(now: LocalDateTime, target: LocalDateTime): Boolean {
            if (target.isBefore(now)) return false

            return Duration.between(now, target).toDays() < IMMINENT_DAYS
        }
    }
}
