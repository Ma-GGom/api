package com.maggom.auth.port.out

interface AuthTokenStoragePort {
    fun save(token: String, email: String)
    fun findEmailByToken(token: String): String?
    fun delete(token: String)
}
