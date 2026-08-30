package com.maggom.member.port.`in`

interface UnsubscribeUseCase {
    fun unsubscribe(token: String): Boolean
}
