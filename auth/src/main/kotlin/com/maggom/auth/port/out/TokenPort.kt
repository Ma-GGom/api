package com.maggom.auth.port.out

interface TokenPort {
    fun generateToken(email: String, role: String): String
    fun extractEmail(token: String): String?
    fun extractRole(token: String): String?
}
