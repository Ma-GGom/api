package com.maggom.app.unsubscribe

import com.maggom.app.adapter.UnsubscribeTokenAdapter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnsubscribeTokenAdapterTest {

    private val adapter = UnsubscribeTokenAdapter("test-unsubscribe-secret-key-at-least-32-chars!!")

    @Test
    @DisplayName("생성한 토큰에서 원본 이메일 추출")
    fun generated_token_returns_original_email() {
        // given
        val email = "user+tag@test.com"

        // when
        val token = adapter.generate(email)

        // then
        assertEquals(email, adapter.extractEmail(token))
    }

    @Test
    @DisplayName("토큰은 URL 안전 문자만 포함")
    fun token_contains_only_url_safe_characters() {
        // given
        val token = adapter.generate("user+tag@test.com")

        // when
        val illegal = token.filterNot { it.isLetterOrDigit() || it in "-_." }

        // then
        assertEquals("", illegal)
    }

    @Test
    @DisplayName("서명이 위조된 토큰은 거부")
    fun tampered_signature_is_rejected() {
        // given
        val token = adapter.generate("user@test.com")
        val payload = token.substringBefore(".")

        // when
        val result = adapter.extractEmail("$payload.forged-signature")

        // then
        assertNull(result)
    }

    @Test
    @DisplayName("payload가 바뀐 토큰은 거부")
    fun tampered_payload_is_rejected() {
        // given
        val token = adapter.generate("user@test.com")
        val otherPayload = adapter.generate("attacker@test.com").substringBefore(".")

        // when
        val result = adapter.extractEmail("$otherPayload.${token.substringAfter(".")}")

        // then
        assertNull(result)
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 거부")
    fun malformed_token_is_rejected() {
        assertNull(adapter.extractEmail(""))
        assertNull(adapter.extractEmail("no-separator"))
        assertNull(adapter.extractEmail("a.b.c"))
    }

    @Test
    @DisplayName("다른 시크릿으로 생성한 토큰은 거부")
    fun token_from_another_secret_is_rejected() {
        // given
        val other = UnsubscribeTokenAdapter("another-secret-key-at-least-32-characters!!")
        val token = other.generate("user@test.com")

        // when
        val result = adapter.extractEmail(token)

        // then
        assertNull(result)
    }
}
