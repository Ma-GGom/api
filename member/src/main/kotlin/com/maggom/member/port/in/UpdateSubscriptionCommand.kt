package com.maggom.member.port.`in`

import java.time.LocalTime

data class UpdateSubscriptionCommand(
    val email: String,
    val receiveDays: String?,
    val receiveTime: LocalTime?,
    val prefRegions: List<String>?,
    val prefDistances: List<String>?,
    val includeSmall: Boolean?,
)
