package com.maggom.member.port.`in`

interface SubscriptionQueryUseCase {
    fun getByEmail(email: String): SubscriptionResult
}
