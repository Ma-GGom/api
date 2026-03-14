package com.maggom.member.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MemberTest {

    @Test
    fun `유효한 이메일로 생성 성공`() {
        assertDoesNotThrow {
            Member(email = "user@example.com")
        }
    }

    @Test
    fun `이메일이 빈 문자열이면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            Member(email = "")
        }

        assertEquals("이메일은 필수입니다.", ex.message)
    }

    @Test
    fun `이메일 형식이 올바르지 않으면 예외`() {
        val ex = assertThrows<IllegalArgumentException> {
            Member(email = "not-an-email")
        }

        assertEquals("올바른 이메일 형식이 아닙니다.", ex.message)
    }

    @Test
    fun `@ 없는 이메일이면 예외`() {
        assertThrows<IllegalArgumentException> {
            Member(email = "userexample.com")
        }
    }

    @Test
    fun `도메인 없는 이메일이면 예외`() {
        assertThrows<IllegalArgumentException> {
            Member(email = "user@")
        }
    }

    @Test
    fun `기본 role은 USER`() {
        val member = Member(email = "user@example.com")

        assertEquals(MemberRole.USER, member.role)
    }

    @Test
    fun `기본 isVerified는 true`() {
        val member = Member(email = "user@example.com")

        assertEquals(true, member.isVerified)
    }
}
