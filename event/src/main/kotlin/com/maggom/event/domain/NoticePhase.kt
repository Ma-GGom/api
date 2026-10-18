package com.maggom.event.domain

import java.time.LocalDateTime

/**
 * 알림 메일에서 대회를 보여 줄 접수 단계. 선언 순서가 노출 우선순위다.
 *
 * 수집 데이터로는 선착순 조기 마감을 알 수 없으므로, 정보가 확실하고 아직 신청할 수 있을 가능성이
 * 높은 단계를 앞에 둔다. 접수중 대회는 연 지 오래될수록 이미 찼을 가능성이 커서 맨 뒤로 보낸다.
 *
 * 상태값은 수집 시점 기준이라, 발송 시점의 날짜로 다시 판단한다.
 */
enum class NoticePhase(val priority: Boolean) {
    /** 7일 안에 접수 시작 - 인기 대회는 오픈 당일 마감되므로 가장 가치가 높다. */
    OPENING_SOON(true),

    /** 접수 시작 14일 이내 - 아직 자리가 남아 있을 가능성이 높다. */
    NEWLY_OPENED(true),

    /** 7일 안에 접수 마감 - 지금 신청해야 하지만 이미 찼을 가능성도 가장 높다. */
    CLOSING_SOON(true),

    /** 접수 시작까지 7일 넘게 남음 */
    UPCOMING(false),

    /** 오래전에 열린 접수중 */
    OPEN(false),
    ;

    companion object {
        private const val IMMINENT_DAYS = 7L
        private const val NEWLY_OPENED_DAYS = 14L

        fun of(event: MarathonEvent, now: LocalDateTime): NoticePhase {
            val imminent = now.plusDays(IMMINENT_DAYS)
            // 시작일을 모르는 대회는 마감일과 같은 값으로 저장되므로 시작일로 판단하지 않는다.
            val startKnown = event.regStartDate != event.regEndDate

            if (startKnown && event.regStartDate.isAfter(now)) {
                return if (event.regStartDate.isAfter(imminent)) UPCOMING else OPENING_SOON
            }

            val regEndDate = event.regEndDate

            if (regEndDate != null && !regEndDate.isAfter(imminent)) return CLOSING_SOON

            if (startKnown && !event.regStartDate.isBefore(now.minusDays(NEWLY_OPENED_DAYS))) return NEWLY_OPENED

            return OPEN
        }
    }
}
