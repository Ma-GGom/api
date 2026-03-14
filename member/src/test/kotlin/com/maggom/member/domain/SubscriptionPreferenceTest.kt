package com.maggom.member.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalTime
import kotlin.test.assertEquals

class SubscriptionPreferenceTest {

    @Test
    fun `기본값으로 생성 성공`() {
        assertDoesNotThrow {
            SubscriptionPreference(memberId = 1L)
        }
    }

    @Test
    fun `유효한 요일 조합으로 생성 성공`() {
        assertDoesNotThrow {
            SubscriptionPreference(memberId = 1L, receiveDays = "MON,WED,FRI")
        }
    }

    @Test
    fun `ALL 단독으로 생성 성공`() {
        assertDoesNotThrow {
            SubscriptionPreference(memberId = 1L, receiveDays = "ALL")
        }
    }

    @Test
    fun `receiveDays가 빈 문자열이면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, receiveDays = "")
        }

        assertEquals("수신 요일은 필수입니다.", ex.message)
    }

    @Test
    fun `유효하지 않은 요일 코드면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, receiveDays = "MONDAY")
        }

        assertEquals(true, ex.message!!.contains("유효하지 않은 수신 요일입니다"))
    }

    @Test
    fun `허용된 수신 시간(08, 12, 18시)으로 생성 성공`() {
        assertDoesNotThrow { SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(8, 0)) }
        assertDoesNotThrow { SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(12, 0)) }
        assertDoesNotThrow { SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(18, 0)) }
    }

    @Test
    fun `허용되지 않은 수신 시간이면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(9, 0))
        }

        assertEquals(true, ex.message!!.contains("유효하지 않은 수신 시간입니다"))
    }

    @Test
    fun `prefRegions가 비어있으면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, prefRegions = emptyList())
        }

        assertEquals("선호 지역은 하나 이상 선택해야 합니다.", ex.message)
    }

    @Test
    fun `prefDistances가 비어있으면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, prefDistances = emptyList())
        }

        assertEquals("선호 코스는 하나 이상 선택해야 합니다.", ex.message)
    }

    @Test
    fun `기본 receiveDays는 MON,WED,FRI`() {
        val pref = SubscriptionPreference(memberId = 1L)

        assertEquals("MON,WED,FRI", pref.receiveDays)
    }

    @Test
    fun `기본 receiveTime은 08시`() {
        val pref = SubscriptionPreference(memberId = 1L)

        assertEquals(LocalTime.of(8, 0), pref.receiveTime)
    }

    @Test
    fun `copy를 통한 부분 업데이트 시 나머지 필드 유지`() {
        val original = SubscriptionPreference(memberId = 1L)
        val updated = original.copy(receiveDays = "SAT,SUN")

        assertEquals("SAT,SUN", updated.receiveDays)
        assertEquals(original.receiveTime, updated.receiveTime)
        assertEquals(original.prefRegions, updated.prefRegions)
        assertEquals(original.prefDistances, updated.prefDistances)
    }
}
