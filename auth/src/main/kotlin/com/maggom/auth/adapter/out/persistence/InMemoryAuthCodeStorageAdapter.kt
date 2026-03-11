package com.maggom.auth.adapter.out.persistence

import com.maggom.auth.port.out.AuthCodeEntry
import com.maggom.auth.port.out.AuthCodeStoragePort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryAuthCodeStorageAdapter : AuthCodeStoragePort {

    private val storage = ConcurrentHashMap<String, AuthCodeEntry>()

    override fun save(email: String, entry: AuthCodeEntry) {
        storage[email] = entry
    }

    override fun findByEmail(email: String): AuthCodeEntry? {
        val entry = storage[email] ?: return null

        return if (entry.expiresAt.isAfter(LocalDateTime.now())) {
            entry
        } else {
            storage.remove(email)
            null
        }
    }

    override fun delete(email: String) {
        storage.remove(email)
    }

    @Scheduled(fixedRate = 60000)
    fun cleanupExpiredCodes() {
        val now = LocalDateTime.now()
        storage.entries.removeIf { it.value.expiresAt.isBefore(now) }
    }
}
