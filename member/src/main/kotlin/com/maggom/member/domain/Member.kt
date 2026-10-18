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
            require(NAME_REGEX.matches(it)) { NAME_FORMAT_MESSAGE }
        }
    }

    /** 앞뒤 공백을 지운 이름으로 바꾼다. 빈 값이면 이름을 지운다. */
    fun rename(newName: String): Member = copy(name = newName.trim().ifEmpty { null })

    companion object {
        const val MAX_NAME_LENGTH = 20

        // 완성형 한글만 허용한다. 영문·숫자·공백·기호·자모 단독(ㅋ 등)은 받지 않는다. (프론트 member-name.ts와 같은 규칙)
        private val NAME_REGEX = Regex("^[가-힣]+$")
        private const val NAME_FORMAT_MESSAGE = "이름은 공백 없이 한글로만 입력해 주세요."
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    }
}
