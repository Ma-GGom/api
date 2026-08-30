package com.maggom.member.application

import com.maggom.common.exception.MemberNotFoundException
import com.maggom.member.port.`in`.SubscriptionDeleteUseCase
import com.maggom.member.port.out.UnsubscribeTokenPort
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnsubscribeServiceTest {

    private val unsubscribeTokenPort: UnsubscribeTokenPort = mockk()
    private val subscriptionDeleteUseCase: SubscriptionDeleteUseCase = mockk()

    private val service = UnsubscribeService(unsubscribeTokenPort, subscriptionDeleteUseCase)

    @Test
    @DisplayName("유효한 토큰이면 구독을 해지하고 true 반환")
    fun valid_token_deletes_subscription() {
        // given
        every { unsubscribeTokenPort.extractEmail("valid-token") } returns "user@test.com"
        justRun { subscriptionDeleteUseCase.delete("user@test.com") }

        // when
        val result = service.unsubscribe("valid-token")

        // then
        assertTrue(result)
        verify(exactly = 1) { subscriptionDeleteUseCase.delete("user@test.com") }
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 해지하지 않고 false 반환")
    fun invalid_token_returns_false() {
        // given
        every { unsubscribeTokenPort.extractEmail("invalid-token") } returns null

        // when
        val result = service.unsubscribe("invalid-token")

        // then
        assertFalse(result)
        verify(exactly = 0) { subscriptionDeleteUseCase.delete(any()) }
    }

    @Test
    @DisplayName("이미 해지된 회원이면 멱등하게 true 반환")
    fun already_unsubscribed_member_returns_true() {
        // given
        every { unsubscribeTokenPort.extractEmail("valid-token") } returns "gone@test.com"
        every { subscriptionDeleteUseCase.delete("gone@test.com") } throws MemberNotFoundException()

        // when
        val result = service.unsubscribe("valid-token")

        // then
        assertTrue(result)
    }
}
