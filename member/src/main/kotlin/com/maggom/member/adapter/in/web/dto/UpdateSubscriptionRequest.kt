package com.maggom.member.adapter.`in`.web.dto

import java.time.LocalTime

data class UpdateSubscriptionRequest(
    val receiveDays: String?,
    val receiveTime: LocalTime?,
    val prefRegions: List<String>?,
    val prefDistances: List<String>?,
    val includeSmall: Boolean?,
    /** 보내지 않거나 null이면 그대로 두고, 빈 문자열이면 이름을 지운다. */
    val name: String? = null,
)
