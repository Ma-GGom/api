package com.maggom.auth.port.out

interface MemberCheckPort {
    fun existsByEmail(email: String): Boolean
    fun findRoleByEmail(email: String): String
}
