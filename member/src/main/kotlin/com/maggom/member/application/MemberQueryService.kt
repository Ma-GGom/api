package com.maggom.member.application

import com.maggom.member.port.`in`.MemberQueryUseCase
import org.springframework.stereotype.Service

@Service
class MemberQueryService : MemberQueryUseCase {

    override fun existsByEmail(email: String): Boolean {
        // TODO: JPA 구현체로 교체
        return false
    }
}
