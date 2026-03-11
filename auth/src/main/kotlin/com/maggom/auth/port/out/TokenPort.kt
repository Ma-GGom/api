package com.maggom.auth.port.out

interface TokenPort {
    fun generateToken(email: String): String
}
