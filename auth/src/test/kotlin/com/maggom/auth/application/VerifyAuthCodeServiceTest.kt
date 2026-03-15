package com.maggom.auth.application

import com.maggom.auth.port.`in`.AuthFlow
import com.maggom.auth.port.`in`.VerifyAuthCodeCommand
import com.maggom.auth.port.out.AuthCodeEntry
import com.maggom.auth.port.out.AuthCodeStoragePort
import com.maggom.auth.port.out.MemberCheckPort
import com.maggom.auth.port.out.MemberRegistrationPort
import com.maggom.auth.port.out.TokenPort
import com.maggom.auth.port.out.WelcomeMailPort
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

class VerifyAuthCodeServiceTest {

    private val authCodeStoragePort: AuthCodeStoragePort = mockk()
    private val tokenPort: TokenPort = mockk()
    private val memberCheckPort: MemberCheckPort = mockk()
    private val memberRegistrationPort: MemberRegistrationPort = mockk()
    private val welcomeMailPort: WelcomeMailPort = mockk()

    private lateinit var service: VerifyAuthCodeService

    @BeforeEach
    fun setUp() {
        service = VerifyAuthCodeService(
            authCodeStoragePort = authCodeStoragePort,
            tokenPort = tokenPort,
            memberCheckPort = memberCheckPort,
            memberRegistrationPort = memberRegistrationPort,
            welcomeMailPort = welcomeMailPort,
        )
    }

    @Test
    @DisplayName("인증 코드가 없으면 IllegalArgumentException")
    fun missing_auth_code_throws_illegal_argument_exception() {
        // given
        every { authCodeStoragePort.findByEmail(any()) } returns null

        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            service.verifyAuthCode(VerifyAuthCodeCommand("user@test.com", "123456"))
        }

        assertEquals("인증 코드가 존재하지 않거나 만료되었습니다.", ex.message)
    }

    @Test
    @DisplayName("인증 코드가 틀리면 IllegalArgumentException")
    fun wrong_auth_code_throws_illegal_argument_exception() {
        // given
        every { authCodeStoragePort.findByEmail(any()) } returns authCodeEntry("999999", AuthFlow.SUBSCRIBE)

        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            service.verifyAuthCode(VerifyAuthCodeCommand("user@test.com", "123456"))
        }

        assertEquals("인증 코드가 올바르지 않습니다.", ex.message)
    }

    @Test
    @DisplayName("SUBSCRIBE 플로우 성공 - 코드 삭제 + 회원 등록 + 웰컴메일 + 토큰 반환")
    fun subscribe_flow_success_deletes_code_registers_member_sends_welcome_mail_and_returns_token() {
        // given
        every { authCodeStoragePort.findByEmail(any()) } returns authCodeEntry("123456", AuthFlow.SUBSCRIBE)
        justRun { authCodeStoragePort.delete(any()) }
        justRun { memberRegistrationPort.register(any()) }
        justRun { welcomeMailPort.sendWelcomeMail(any()) }
        every { tokenPort.generateToken(any(), any()) } returns "jwt.token.value"

        // when
        val result = service.verifyAuthCode(VerifyAuthCodeCommand("user@test.com", "123456"))

        // then
        assertEquals("jwt.token.value", result.accessToken)
        verify(exactly = 1) { authCodeStoragePort.delete("user@test.com") }
        verify(exactly = 1) { memberRegistrationPort.register("user@test.com") }
        verify(exactly = 1) { welcomeMailPort.sendWelcomeMail("user@test.com") }
    }

    @Test
    @DisplayName("SETTINGS 플로우 성공 - 회원 등록 및 웰컴메일 호출 없음")
    fun settings_flow_success_does_not_register_member_or_send_welcome_mail() {
        // given
        every { authCodeStoragePort.findByEmail(any()) } returns authCodeEntry("123456", AuthFlow.SETTINGS)
        justRun { authCodeStoragePort.delete(any()) }
        every { memberCheckPort.findRoleByEmail(any()) } returns "MEMBER"
        every { tokenPort.generateToken(any(), any()) } returns "jwt.token.value"

        // when
        val result = service.verifyAuthCode(VerifyAuthCodeCommand("user@test.com", "123456"))

        // then
        assertEquals("jwt.token.value", result.accessToken)
        verify(exactly = 0) { memberRegistrationPort.register(any()) }
        verify(exactly = 0) { welcomeMailPort.sendWelcomeMail(any()) }
    }

    @Test
    @DisplayName("인증 성공 시 코드는 1회만 삭제")
    fun auth_success_deletes_code_exactly_once() {
        // given
        every { authCodeStoragePort.findByEmail(any()) } returns authCodeEntry("123456", AuthFlow.SETTINGS)
        justRun { authCodeStoragePort.delete(any()) }
        every { memberCheckPort.findRoleByEmail(any()) } returns "MEMBER"
        every { tokenPort.generateToken(any(), any()) } returns "token"

        // when
        service.verifyAuthCode(VerifyAuthCodeCommand("user@test.com", "123456"))

        // then
        verify(exactly = 1) { authCodeStoragePort.delete("user@test.com") }
    }

    private fun authCodeEntry(code: String, flow: AuthFlow) = AuthCodeEntry(
        code = code,
        expiresAt = LocalDateTime.now().plusMinutes(3),
        sentAt = LocalDateTime.now(),
        flow = flow,
    )
}
