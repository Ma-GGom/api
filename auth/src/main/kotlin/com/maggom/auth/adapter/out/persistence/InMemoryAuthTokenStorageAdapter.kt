package com.maggom.auth.adapter.out.persistence

import com.maggom.auth.port.out.AuthTokenStoragePort
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryAuthTokenStorageAdapter : AuthTokenStoragePort {

    private val storage = ConcurrentHashMap<String, String>()

    override fun save(token: String, email: String) {
        storage[token] = email
    }

    override fun findEmailByToken(token: String): String? {
        return storage[token]
    }

    override fun delete(token: String) {
        storage.remove(token)
    }
}
