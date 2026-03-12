package com.maggom.auth.port.out

interface MemberCheckPort {
    fun existsByEmail(email: String): Boolean
}
