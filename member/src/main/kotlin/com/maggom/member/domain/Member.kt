package com.maggom.member.domain

import java.time.LocalDateTime

data class Member(
    val id: Long = 0,
    val email: String,
    val role: MemberRole = MemberRole.MEMBER,
    val isVerified: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    init {
        require(email.isNotBlank()) { "이메일은 필수입니다." }
        require(EMAIL_REGEX.matches(email)) { "올바른 이메일 형식이 아닙니다." }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    }
}
