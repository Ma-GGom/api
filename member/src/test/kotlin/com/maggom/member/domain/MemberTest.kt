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
    @DisplayName("이름은 선택값이고 기본은 없음")
    fun name_is_optional() {
        // when
        val member = Member(email = "user@example.com")

        // then
        assertEquals(null, member.name)
    }

    @Test
    @DisplayName("이름이 20자를 넘으면 예외")
    fun too_long_name_throws_exception() {
        // when & then
        val ex = assertThrows<IllegalArgumentException> {
            Member(email = "user@example.com", name = "가".repeat(21))
        }

        assertEquals("이름은 20자 이하로 입력해 주세요.", ex.message)
    }

    @Test
    @DisplayName("이름은 완성형 한글만 허용")
    fun korean_name_is_allowed() {
        // when & then
        listOf("김마꼼", "남궁민수", "가").forEach { name ->
            assertDoesNotThrow { Member(email = "user@example.com", name = name) }
        }
    }

    @Test
    @DisplayName("이름에 한글 외 문자가 있으면 예외")
    fun non_korean_name_throws_exception() {
        // when & then
        listOf("Kim", "러너123", "김 마꼼", "ㅋㅋㅋ", "김마꼼😀", "<script>", "김·마꼼").forEach { name ->
            val ex = assertThrows<IllegalArgumentException> { Member(email = "user@example.com", name = name) }

            assertEquals("이름은 공백 없이 한글로만 입력해 주세요.", ex.message)
        }
    }

    @Test
    @DisplayName("rename - 앞뒤 공백을 지우고, 빈 값이면 이름 삭제")
    fun rename_trims_and_clears_blank() {
        // given
        val member = Member(email = "user@example.com", name = "김마꼼")

        // when & then
        assertEquals("박러너", member.rename("  박러너 ").name)
        assertEquals(null, member.rename("   ").name)
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
