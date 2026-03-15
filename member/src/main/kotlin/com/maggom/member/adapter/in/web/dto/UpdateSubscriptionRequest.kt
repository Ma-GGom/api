package com.maggom.member.adapter.`in`.web.dto

import java.time.LocalTime

data class UpdateSubscriptionRequest(
    val receiveDays: String?,
    val receiveTime: LocalTime?,
    val prefRegions: List<String>?,
    val prefDistances: List<String>?,
    val includeSmall: Boolean?,
)
