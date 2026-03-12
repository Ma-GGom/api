package com.maggom.member.port.`in`

interface MemberQueryUseCase {
    fun existsByEmail(email: String): Boolean
}
