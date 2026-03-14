package com.maggom.member.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MemberTest {

    @Test
    @DisplayName("유효한 이메일로 생성 성공")
    fun valid_email_creates_member_successfully() {
        // when & then
        assertDoesNotThrow {
            Member(email = "user@example.com")
        }
    }

    @Test
    @DisplayName("이메일이 빈 문자열이면 예외")
    fun empty_email_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            Member(email = "")
        }

        assertEquals("이메일은 필수입니다.", ex.message)
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 예외")
    fun invalid_email_format_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            Member(email = "not-an-email")
        }

        assertEquals("올바른 이메일 형식이 아닙니다.", ex.message)
    }

    @Test
    @DisplayName("@ 없는 이메일이면 예외")
    fun email_without_at_sign_throws_exception() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "userexample.com")
        }
    }

    @Test
    @DisplayName("도메인 없는 이메일이면 예외")
    fun email_without_domain_throws_exception() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "user@")
        }
    }

    @Test
    @DisplayName("기본 role은 USER")
    fun default_role_is_user() {
        // when
        val member = Member(email = "user@example.com")

        // then
        assertEquals(MemberRole.MEMBER, member.role)
    }

    @Test
    @DisplayName("기본 isVerified는 true")
    fun default_is_verified_is_true() {
        // when
        val member = Member(email = "user@example.com")

        // then
        assertEquals(true, member.isVerified)
    }
}
