package com.maggom.member.port.`in`

import java.time.LocalTime

data class UpdateSubscriptionCommand(
    val email: String,
    val receiveDays: String?,
    val receiveTime: LocalTime?,
    val prefRegions: List<String>?,
    val prefDistances: List<String>?,
    val includeSmall: Boolean?,
    /** null이면 그대로 두고, 빈 문자열이면 이름을 지운다. */
    val name: String? = null,
)
