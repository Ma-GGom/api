package com.maggom.member.application

import com.maggom.common.exception.MemberNotFoundException
import com.maggom.member.domain.Member
import com.maggom.member.domain.SubscriptionPreference
import com.maggom.member.port.`in`.UpdateSubscriptionCommand
import com.maggom.member.port.out.MemberPort
import com.maggom.member.port.out.SubscriptionPreferencePort
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubscriptionServiceTest {

    private val memberPort: MemberPort = mockk()
    private val subscriptionPreferencePort: SubscriptionPreferencePort = mockk()

    private lateinit var service: SubscriptionService

    @BeforeEach
    fun setUp() {
        service = SubscriptionService(
            memberPort = memberPort,
            subscriptionPreferencePort = subscriptionPreferencePort,
        )
    }

    // ── getByEmail ──────────────────────────────────────────────────────────

    @Test
    fun `getByEmail - 성공 시 구독 설정 반환`() {
        val member = member(id = 1L)
        val pref = pref(memberId = 1L)
        every { memberPort.findByEmail("user@test.com") } returns member
        every { subscriptionPreferencePort.findByMemberId(1L) } returns pref

        val result = service.getByEmail("user@test.com")

        assertEquals("MON,WED,FRI", result.receiveDays)
        assertEquals(listOf("수도권"), result.prefRegions)
        assertEquals(listOf("10K", "HALF"), result.prefDistances)
    }

    @Test
    fun `getByEmail - 회원이 없으면 MemberNotFoundException`() {
        every { memberPort.findByEmail(any()) } returns null

        assertThrows<MemberNotFoundException> {
            service.getByEmail("none@test.com")
        }
    }

    @Test
    fun `getByEmail - 구독 설정이 없으면 MemberNotFoundException`() {
        every { memberPort.findByEmail(any()) } returns member(id = 1L)
        every { subscriptionPreferencePort.findByMemberId(any()) } returns null

        assertThrows<MemberNotFoundException> {
            service.getByEmail("user@test.com")
        }
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    fun `update - null 필드는 기존 값 유지`() {
        val member = member(id = 1L)
        val existingPref = pref(memberId = 1L)
        val savedSlot = slot<SubscriptionPreference>()

        every { memberPort.findByEmail(any()) } returns member
        every { subscriptionPreferencePort.findByMemberId(1L) } returns existingPref
        every { subscriptionPreferencePort.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.update(
            UpdateSubscriptionCommand(
                email = "user@test.com",
                receiveDays = "MON,FRI",
                receiveTime = null,
                prefRegions = null,
                prefDistances = null,
                includeSmall = null,
            )
        )

        assertEquals("MON,FRI", savedSlot.captured.receiveDays)
        assertEquals(existingPref.receiveTime, savedSlot.captured.receiveTime)
        assertEquals(existingPref.prefRegions, savedSlot.captured.prefRegions)
        assertEquals(existingPref.prefDistances, savedSlot.captured.prefDistances)
        assertEquals(existingPref.includeSmall, savedSlot.captured.includeSmall)
    }

    @Test
    fun `update - 전체 필드 변경`() {
        val member = member(id = 1L)
        val savedSlot = slot<SubscriptionPreference>()

        every { memberPort.findByEmail(any()) } returns member
        every { subscriptionPreferencePort.findByMemberId(1L) } returns pref(memberId = 1L)
        every { subscriptionPreferencePort.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.update(
            UpdateSubscriptionCommand(
                email = "user@test.com",
                receiveDays = "SAT,SUN",
                receiveTime = LocalTime.of(12, 0),
                prefRegions = listOf("충청권"),
                prefDistances = listOf("FULL"),
                includeSmall = false,
            )
        )

        assertEquals("SAT,SUN", savedSlot.captured.receiveDays)
        assertEquals(LocalTime.of(12, 0), savedSlot.captured.receiveTime)
        assertEquals(listOf("충청권"), savedSlot.captured.prefRegions)
        assertEquals(listOf("FULL"), savedSlot.captured.prefDistances)
        assertEquals(false, savedSlot.captured.includeSmall)
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    fun `delete - 구독 설정 삭제 후 회원 삭제`() {
        val callOrder = mutableListOf<String>()
        val member = member(id = 1L)

        every { memberPort.findByEmail(any()) } returns member
        every { subscriptionPreferencePort.deleteByMemberId(1L) } answers { callOrder.add("deletePref") }
        every { memberPort.deleteById(1L) } answers { callOrder.add("deleteMember") }

        service.delete("user@test.com")

        assertEquals(listOf("deletePref", "deleteMember"), callOrder)
    }

    @Test
    fun `delete - 회원이 없으면 MemberNotFoundException`() {
        every { memberPort.findByEmail(any()) } returns null

        assertThrows<MemberNotFoundException> {
            service.delete("none@test.com")
        }
    }

    // ── count ────────────────────────────────────────────────────────────────

    @Test
    fun `count - memberPort countAll 값을 그대로 반환`() {
        every { memberPort.countAll() } returns 42L

        assertEquals(42L, service.count())
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun member(id: Long) = Member(
        id = id,
        email = "user@test.com",
    )

    private fun pref(memberId: Long) = SubscriptionPreference(
        memberId = memberId,
        receiveDays = "MON,WED,FRI",
        receiveTime = LocalTime.of(8, 0),
        prefRegions = listOf("수도권"),
        prefDistances = listOf("10K", "HALF"),
        includeSmall = true,
    )
}
