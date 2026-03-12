package com.maggom.member.domain

import java.time.LocalTime

data class SubscriptionPreference(
    val id: Long = 0,
    val memberId: Long,
    val receiveDays: String = "MON,WED,FRI",
    val receiveTime: LocalTime = LocalTime.of(8, 0),
    val prefRegions: List<String> = listOf("수도권"),
    val prefDistances: List<String> = listOf("10K", "HALF"),
    val includeSmall: Boolean = true,
) {
    init {
        require(receiveDays.isNotBlank()) { "수신 요일은 필수입니다." }
        require(receiveDays.split(",").all { it.trim() in VALID_DAYS }) {
            "유효하지 않은 수신 요일입니다. 허용값: ${VALID_DAYS.joinToString()}"
        }
        require(prefRegions.isNotEmpty()) { "선호 지역은 하나 이상 선택해야 합니다." }
        require(prefDistances.isNotEmpty()) { "선호 코스는 하나 이상 선택해야 합니다." }
    }

    companion object {
        val VALID_DAYS = setOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN", "ALL")
    }
}
