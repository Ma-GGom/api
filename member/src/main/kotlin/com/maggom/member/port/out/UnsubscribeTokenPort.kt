package com.maggom.member.port.out

interface UnsubscribeTokenPort {
    fun generate(email: String): String
    fun extractEmail(token: String): String?
}
