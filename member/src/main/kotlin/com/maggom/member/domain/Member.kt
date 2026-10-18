package com.maggom.member.domain

import java.time.LocalDateTime

data class Member(
    val id: Long = 0,
    val email: String,
    val name: String? = null,
    val role: MemberRole = MemberRole.MEMBER,
    val isVerified: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    init {
        require(email.isNotBlank()) { "이메일은 필수입니다." }
        require(EMAIL_REGEX.matches(email)) { "올바른 이메일 형식이 아닙니다." }
        name?.let {
            require(it.isNotBlank()) { "이름은 공백일 수 없습니다." }
            require(it.length <= MAX_NAME_LENGTH) { "이름은 ${MAX_NAME_LENGTH}자 이하로 입력해 주세요." }
        }
    }

    /** 앞뒤 공백을 지운 이름으로 바꾼다. 빈 값이면 이름을 지운다. */
    fun rename(newName: String): Member = copy(name = newName.trim().ifEmpty { null })

    companion object {
        const val MAX_NAME_LENGTH = 20
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    }
}
