package com.maggom.auth.port.out

import java.time.LocalDateTime

interface AuthCodeStoragePort {
    fun save(email: String, entry: AuthCodeEntry)
    fun findByEmail(email: String): AuthCodeEntry?
    fun delete(email: String)
}

data class AuthCodeEntry(
    val code: String,
    val expiresAt: LocalDateTime,
    val sentAt: LocalDateTime,
)
