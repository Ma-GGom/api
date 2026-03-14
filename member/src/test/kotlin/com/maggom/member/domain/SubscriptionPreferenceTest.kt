package com.maggom.member.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalTime
import kotlin.test.assertEquals

class SubscriptionPreferenceTest {

    @Test
    @DisplayName("기본값으로 생성 성공")
    fun default_values_create_subscription_preference_successfully() {
        // when & then
        assertDoesNotThrow {
            SubscriptionPreference(memberId = 1L)
        }
    }

    @Test
    @DisplayName("유효한 요일 조합으로 생성 성공")
    fun valid_receive_days_combination_creates_successfully() {
        // when & then
        assertDoesNotThrow {
            SubscriptionPreference(memberId = 1L, receiveDays = "MON,WED,FRI")
        }
    }

    @Test
    @DisplayName("ALL 단독으로 생성 성공")
    fun all_receive_days_creates_successfully() {
        // when & then
        assertDoesNotThrow {
            SubscriptionPreference(memberId = 1L, receiveDays = "ALL")
        }
    }

    @Test
    @DisplayName("receiveDays가 빈 문자열이면 예외")
    fun empty_receive_days_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, receiveDays = "")
        }

        assertEquals("수신 요일은 필수입니다.", ex.message)
    }

    @Test
    @DisplayName("유효하지 않은 요일 코드면 예외")
    fun invalid_day_code_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, receiveDays = "MONDAY")
        }

        assertEquals(true, ex.message!!.contains("유효하지 않은 수신 요일입니다"))
    }

    @Test
    @DisplayName("허용된 수신 시간(08, 12, 18시)으로 생성 성공")
    fun allowed_receive_times_create_successfully() {
        // when & then
        assertDoesNotThrow { SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(8, 0)) }
        assertDoesNotThrow { SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(12, 0)) }
        assertDoesNotThrow { SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(18, 0)) }
    }

    @Test
    @DisplayName("허용되지 않은 수신 시간이면 예외")
    fun disallowed_receive_time_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, receiveTime = LocalTime.of(9, 0))
        }

        assertEquals(true, ex.message!!.contains("유효하지 않은 수신 시간입니다"))
    }

    @Test
    @DisplayName("prefRegions가 비어있으면 예외")
    fun empty_pref_regions_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, prefRegions = emptyList())
        }

        assertEquals("선호 지역은 하나 이상 선택해야 합니다.", ex.message)
    }

    @Test
    @DisplayName("prefDistances가 비어있으면 예외")
    fun empty_pref_distances_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            SubscriptionPreference(memberId = 1L, prefDistances = emptyList())
        }

        assertEquals("선호 코스는 하나 이상 선택해야 합니다.", ex.message)
    }

    @Test
    @DisplayName("기본 receiveDays는 MON,WED,FRI")
    fun default_receive_days_is_mon_wed_fri() {
        // when
        val pref = SubscriptionPreference(memberId = 1L)

        // then
        assertEquals("MON,WED,FRI", pref.receiveDays)
    }

    @Test
    @DisplayName("기본 receiveTime은 08시")
    fun default_receive_time_is_08_00() {
        // when
        val pref = SubscriptionPreference(memberId = 1L)

        // then
        assertEquals(LocalTime.of(8, 0), pref.receiveTime)
    }

    @Test
    @DisplayName("copy를 통한 부분 업데이트 시 나머지 필드 유지")
    fun partial_update_via_copy_preserves_other_fields() {
        // given
        val original = SubscriptionPreference(memberId = 1L)

        // when
        val updated = original.copy(receiveDays = "SAT,SUN")

        // then
        assertEquals("SAT,SUN", updated.receiveDays)
        assertEquals(original.receiveTime, updated.receiveTime)
        assertEquals(original.prefRegions, updated.prefRegions)
        assertEquals(original.prefDistances, updated.prefDistances)
    }
}
