package com.maggom.auth.application

import com.maggom.auth.port.`in`.AuthFlow
import com.maggom.auth.port.`in`.SendAuthCodeCommand
import com.maggom.auth.port.out.AuthCodeEntry
import com.maggom.auth.port.out.AuthCodeStoragePort
import com.maggom.auth.port.out.AuthCodeEmailMessage
import com.maggom.auth.port.out.EmailSenderPort
import com.maggom.auth.port.out.MemberCheckPort
import com.maggom.common.exception.MemberAlreadyExistsException
import com.maggom.common.exception.MemberNotFoundException
import com.maggom.common.exception.TooManyRequestsException
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthCodeServiceTest {

    private val authCodeStoragePort: AuthCodeStoragePort = mockk()
    private val emailSenderPort: EmailSenderPort = mockk()
    private val memberCheckPort: MemberCheckPort = mockk()

    private lateinit var service: AuthCodeService

    @BeforeEach
    fun setUp() {
        service = AuthCodeService(
            authCodeStoragePort = authCodeStoragePort,
            emailSenderPort = emailSenderPort,
            memberCheckPort = memberCheckPort,
            expiryMinutes = 3L,
            cooldownSeconds = 60L,
        )
    }

    @Test
    @DisplayName("SUBSCRIBE 플로우 - 신규 회원이면 코드 저장 후 이메일 발송")
    fun subscribe_flow_new_member_saves_code_and_sends_email() {
        // given
        every { memberCheckPort.existsByEmail(any()) } returns false
        every { authCodeStoragePort.findByEmail(any()) } returns null
        every { authCodeStoragePort.save(any(), any()) } returns Unit
        justRun { emailSenderPort.sendAuthCode(any()) }

        // when
        val result = service.sendAuthCode(SendAuthCodeCommand("new@test.com", AuthFlow.SUBSCRIBE))

        // then
        assertEquals(180L, result.expiresInSeconds)
        verify(exactly = 1) { authCodeStoragePort.save(eq("new@test.com"), any()) }
        verify(exactly = 1) { emailSenderPort.sendAuthCode(any()) }
    }

    @Test
    @DisplayName("SUBSCRIBE 플로우 - 이미 가입된 회원이면 MemberAlreadyExistsException")
    fun subscribe_flow_existing_member_throws_member_already_exists_exception() {
        // given
        every { memberCheckPort.existsByEmail(any()) } returns true

        // when & then
        assertThrows<MemberAlreadyExistsException> {
            service.sendAuthCode(SendAuthCodeCommand("exists@test.com", AuthFlow.SUBSCRIBE))
        }

        verify(exactly = 0) { authCodeStoragePort.save(any(), any()) }
    }

    @Test
    @DisplayName("SETTINGS 플로우 - 기존 회원이면 코드 저장 후 이메일 발송")
    fun settings_flow_existing_member_saves_code_and_sends_email() {
        // given
        every { memberCheckPort.existsByEmail(any()) } returns true
        every { authCodeStoragePort.findByEmail(any()) } returns null
        every { authCodeStoragePort.save(any(), any()) } returns Unit
        justRun { emailSenderPort.sendAuthCode(any()) }

        // when
        val result = service.sendAuthCode(SendAuthCodeCommand("member@test.com", AuthFlow.SETTINGS))

        // then
        assertEquals(180L, result.expiresInSeconds)
        verify(exactly = 1) { emailSenderPort.sendAuthCode(any()) }
    }

    @Test
    @DisplayName("SETTINGS 플로우 - 없는 회원이면 MemberNotFoundException")
    fun settings_flow_nonexistent_member_throws_member_not_found_exception() {
        // given
        every { memberCheckPort.existsByEmail(any()) } returns false

        // when & then
        assertThrows<MemberNotFoundException> {
            service.sendAuthCode(SendAuthCodeCommand("none@test.com", AuthFlow.SETTINGS))
        }
    }

    @Test
    @DisplayName("쿨다운 이내 재요청 시 TooManyRequestsException")
    fun resend_within_cooldown_throws_too_many_requests_exception() {
        // given
        every { memberCheckPort.existsByEmail(any()) } returns false
        every { authCodeStoragePort.findByEmail(any()) } returns AuthCodeEntry(
            code = "123456",
            expiresAt = LocalDateTime.now().plusMinutes(3),
            sentAt = LocalDateTime.now().minusSeconds(10),
            flow = AuthFlow.SUBSCRIBE,
        )

        // when & then
        val ex = assertThrows<TooManyRequestsException> {
            service.sendAuthCode(SendAuthCodeCommand("new@test.com", AuthFlow.SUBSCRIBE))
        }

        assertTrue(ex.message!!.contains("초 후에 재발송 가능합니다"))
    }

    @Test
    @DisplayName("쿨다운 이후 재요청 시 정상 발송")
    fun resend_after_cooldown_sends_email_successfully() {
        // given
        every { memberCheckPort.existsByEmail(any()) } returns false
        every { authCodeStoragePort.findByEmail(any()) } returns AuthCodeEntry(
            code = "123456",
            expiresAt = LocalDateTime.now().plusMinutes(3),
            sentAt = LocalDateTime.now().minusSeconds(61),
            flow = AuthFlow.SUBSCRIBE,
        )
        every { authCodeStoragePort.save(any(), any()) } returns Unit
        justRun { emailSenderPort.sendAuthCode(any()) }

        // when
        service.sendAuthCode(SendAuthCodeCommand("new@test.com", AuthFlow.SUBSCRIBE))

        // then
        verify(exactly = 1) { emailSenderPort.sendAuthCode(any()) }
    }

    @Test
    @DisplayName("이메일 발송 실패 시 저장된 코드 삭제 후 예외 전파")
    fun email_send_failure_deletes_saved_code_and_propagates_exception() {
        // given
        every { memberCheckPort.existsByEmail(any()) } returns false
        every { authCodeStoragePort.findByEmail(any()) } returns null
        every { authCodeStoragePort.save(any(), any()) } returns Unit
        every { authCodeStoragePort.delete(any()) } returns Unit
        every { emailSenderPort.sendAuthCode(any()) } throws RuntimeException("SMTP 오류")

        // when & then
        assertThrows<RuntimeException> {
            service.sendAuthCode(SendAuthCodeCommand("new@test.com", AuthFlow.SUBSCRIBE))
        }

        verify(exactly = 1) { authCodeStoragePort.save(eq("new@test.com"), any()) }
        verify(exactly = 1) { authCodeStoragePort.delete(eq("new@test.com")) }
    }

    @Test
    @DisplayName("저장 후 발송 순서 - 저장이 먼저 호출되고 이후 발송")
    fun save_is_called_before_send() {
        // given
        val callOrder = mutableListOf<String>()

        every { memberCheckPort.existsByEmail(any()) } returns false
        every { authCodeStoragePort.findByEmail(any()) } returns null
        every { authCodeStoragePort.save(any(), any()) } answers { callOrder.add("save") }
        every { emailSenderPort.sendAuthCode(any()) } answers { callOrder.add("send") }

        // when
        service.sendAuthCode(SendAuthCodeCommand("new@test.com", AuthFlow.SUBSCRIBE))

        // then
        assertEquals(listOf("save", "send"), callOrder)
    }
}
