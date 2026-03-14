package com.maggom.app.notification

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.domain.MarathonEventStatus
import com.maggom.event.port.out.MarathonEventPort
import com.maggom.member.domain.Member
import com.maggom.member.port.`in`.SubscriptionQueryUseCase
import com.maggom.member.port.`in`.SubscriptionResult
import com.maggom.member.port.out.MemberPort
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NotificationSchedulerTest {

    private val memberPort: MemberPort = mockk()
    private val subscriptionQueryUseCase: SubscriptionQueryUseCase = mockk()
    private val marathonEventPort: MarathonEventPort = mockk()
    private val notificationMailPort: NotificationMailPort = mockk()

    private lateinit var scheduler: NotificationScheduler

    private val todayCode: String = when (LocalDate.now().dayOfWeek) {
        DayOfWeek.MONDAY -> "MON"
        DayOfWeek.TUESDAY -> "TUE"
        DayOfWeek.WEDNESDAY -> "WED"
        DayOfWeek.THURSDAY -> "THU"
        DayOfWeek.FRIDAY -> "FRI"
        DayOfWeek.SATURDAY -> "SAT"
        DayOfWeek.SUNDAY -> "SUN"
    }
    private val notTodayCode: String = if (todayCode == "MON") "TUE" else "MON"
    private val currentHour: Int = LocalTime.now().hour
    private val differentHour: Int = (currentHour + 1) % 24

    @BeforeEach
    fun setUp() {
        scheduler = NotificationScheduler(
            memberPort = memberPort,
            subscriptionQueryUseCase = subscriptionQueryUseCase,
            marathonEventPort = marathonEventPort,
            notificationMailPort = notificationMailPort,
        )
    }

    @Test
    fun `receiveDays가 ALL이고 시간 일치하면 메일 발송`() {
        every { memberPort.findAll() } returns listOf(member())
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = "ALL",
            receiveTime = LocalTime.of(currentHour, 0),
        )
        every { marathonEventPort.findActiveByRegions(any()) } returns listOf(event(distances = listOf("10K")))
        justRun { notificationMailPort.sendNotification(any(), any()) }

        scheduler.sendNotifications()

        verify(exactly = 1) { notificationMailPort.sendNotification(any(), any()) }
    }

    @Test
    fun `오늘 요일이 수신 요일에 포함되면 메일 발송`() {
        every { memberPort.findAll() } returns listOf(member())
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = todayCode,
            receiveTime = LocalTime.of(currentHour, 0),
        )
        every { marathonEventPort.findActiveByRegions(any()) } returns listOf(event(distances = listOf("10K")))
        justRun { notificationMailPort.sendNotification(any(), any()) }

        scheduler.sendNotifications()

        verify(exactly = 1) { notificationMailPort.sendNotification(any(), any()) }
    }

    @Test
    fun `오늘 요일이 수신 요일에 없으면 메일 미발송`() {
        every { memberPort.findAll() } returns listOf(member())
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = notTodayCode,
            receiveTime = LocalTime.of(currentHour, 0),
        )

        scheduler.sendNotifications()

        verify(exactly = 0) { notificationMailPort.sendNotification(any(), any()) }
    }

    @Test
    fun `수신 시간이 현재 시간과 다르면 메일 미발송`() {
        every { memberPort.findAll() } returns listOf(member())
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = "ALL",
            receiveTime = LocalTime.of(differentHour, 0),
        )

        scheduler.sendNotifications()

        verify(exactly = 0) { notificationMailPort.sendNotification(any(), any()) }
    }

    @Test
    fun `매칭되는 이벤트가 없으면 메일 미발송`() {
        every { memberPort.findAll() } returns listOf(member())
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = "ALL",
            receiveTime = LocalTime.of(currentHour, 0),
        )
        every { marathonEventPort.findActiveByRegions(any()) } returns emptyList()

        scheduler.sendNotifications()

        verify(exactly = 0) { notificationMailPort.sendNotification(any(), any()) }
    }

    @Test
    fun `구독 distances에 맞는 이벤트가 없으면 메일 미발송`() {
        every { memberPort.findAll() } returns listOf(member())
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = "ALL",
            receiveTime = LocalTime.of(currentHour, 0),
            prefDistances = listOf("10K"),
        )
        every { marathonEventPort.findActiveByRegions(any()) } returns listOf(
            event(distances = listOf("FULL")),
        )

        scheduler.sendNotifications()

        verify(exactly = 0) { notificationMailPort.sendNotification(any(), any()) }
    }

    @Test
    fun `구독 distances에 하나라도 일치하면 해당 이벤트 포함하여 발송`() {
        every { memberPort.findAll() } returns listOf(member())
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = "ALL",
            receiveTime = LocalTime.of(currentHour, 0),
            prefDistances = listOf("10K", "HALF"),
        )
        every { marathonEventPort.findActiveByRegions(any()) } returns listOf(
            event(distances = listOf("HALF", "FULL")),
        )
        justRun { notificationMailPort.sendNotification(any(), any()) }

        scheduler.sendNotifications()

        verify(exactly = 1) { notificationMailPort.sendNotification(any(), any()) }
    }

    @Test
    fun `한 회원 발송 실패해도 다음 회원 계속 처리`() {
        val member1 = member(email = "fail@test.com")
        val member2 = member(email = "ok@test.com")

        every { memberPort.findAll() } returns listOf(member1, member2)
        every { subscriptionQueryUseCase.getByEmail(any()) } returns pref(
            receiveDays = "ALL",
            receiveTime = LocalTime.of(currentHour, 0),
        )
        every { marathonEventPort.findActiveByRegions(any()) } returns listOf(event(distances = listOf("10K")))
        every { notificationMailPort.sendNotification("fail@test.com", any()) } throws RuntimeException("발송 실패")
        justRun { notificationMailPort.sendNotification("ok@test.com", any()) }

        scheduler.sendNotifications()

        verify(exactly = 1) { notificationMailPort.sendNotification("ok@test.com", any()) }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun member(email: String = "user@test.com") = Member(
        id = 1L,
        email = email,
    )

    private fun pref(
        receiveDays: String = "ALL",
        receiveTime: LocalTime = LocalTime.of(currentHour, 0),
        prefRegions: List<String> = listOf("수도권"),
        prefDistances: List<String> = listOf("10K", "HALF"),
    ) = SubscriptionResult(
        receiveDays = receiveDays,
        receiveTime = receiveTime,
        prefRegions = prefRegions,
        prefDistances = prefDistances,
        includeSmall = true,
    )

    private fun event(distances: List<String>) = MarathonEvent(
        id = 1L,
        title = "테스트 마라톤",
        eventDate = LocalDate.now().plusMonths(1),
        region = "수도권",
        distances = distances,
        regStartDate = LocalDateTime.now().minusDays(1),
        regEndDate = LocalDateTime.now().plusDays(7),
        linkUrl = "https://example.com",
        status = MarathonEventStatus.OPEN,
        sourceName = "test",
        sourceUrl = "https://source.com",
        crawledAtKst = LocalDateTime.now(),
    )
}
