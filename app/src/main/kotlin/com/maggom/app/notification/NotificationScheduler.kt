package com.maggom.app.notification

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.port.out.MarathonEventPort
import com.maggom.member.port.`in`.SubscriptionQueryUseCase
import com.maggom.member.port.out.MemberPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalTime

@Component
class NotificationScheduler(
    private val memberPort: MemberPort,
    private val subscriptionQueryUseCase: SubscriptionQueryUseCase,
    private val marathonEventPort: MarathonEventPort,
    private val notificationMailPort: NotificationMailPort,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(cron = "0 0 * * * *")
    fun sendNotifications() {
        val now = LocalTime.now()
        val todayCode = toDayCode(java.time.LocalDate.now().dayOfWeek)
        log.info("알림 발송 시작 - 요일: $todayCode, 시간: ${now.hour}시")

        val members = memberPort.findAll()
        var sentCount = 0

        for (member in members) {
            try {
                val pref = subscriptionQueryUseCase.getByEmail(member.email)

                if (!shouldSendToday(pref.receiveDays, todayCode)) continue
                if (!isReceiveHour(pref.receiveTime, now)) continue

                val events = findMatchingEvents(pref.prefRegions, pref.prefDistances)
                if (events.isEmpty()) continue

                notificationMailPort.sendNotification(member.email, events)
                sentCount++

                Thread.sleep(SEND_INTERVAL_MS)
            } catch (e: Exception) {
                log.error("알림 발송 실패 - email: ${member.email}", e)
            }
        }

        log.info("알림 발송 완료 - 발송 수: $sentCount")
    }

    private fun findMatchingEvents(prefRegions: List<String>, prefDistances: List<String>): List<MarathonEvent> {
        return marathonEventPort.findActiveByRegions(prefRegions)
            .filter { event -> event.distances.any { it in prefDistances } }
    }

    private fun shouldSendToday(receiveDays: String, todayCode: String): Boolean {
        val days = receiveDays.split(",").map { it.trim() }

        return "ALL" in days || todayCode in days
    }

    private fun isReceiveHour(receiveTime: LocalTime, now: LocalTime): Boolean {
        return receiveTime.hour == now.hour
    }

    private fun toDayCode(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "MON"
        DayOfWeek.TUESDAY -> "TUE"
        DayOfWeek.WEDNESDAY -> "WED"
        DayOfWeek.THURSDAY -> "THU"
        DayOfWeek.FRIDAY -> "FRI"
        DayOfWeek.SATURDAY -> "SAT"
        DayOfWeek.SUNDAY -> "SUN"
    }

    companion object {
        private const val SEND_INTERVAL_MS = 200L
    }
}
