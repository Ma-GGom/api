package com.maggom.app.adapter

import com.maggom.auth.port.out.TokenPort
import com.maggom.common.exception.TokenExpiredException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenAdapter(
    @Value("\${maggom.auth.jwt-secret}") private val secret: String,
    @Value("\${maggom.auth.access-token-expiry-hours}") private val expiryHours: Long,
) : TokenPort {

    private val key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    override fun generateToken(email: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + expiryHours * 3600 * 1000)

        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    override fun extractEmail(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException()
        } catch (e: JwtException) {
            null
        }
    }

    override fun extractRole(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload["role"] as? String
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException()
        } catch (e: JwtException) {
            null
        }
    }
}
