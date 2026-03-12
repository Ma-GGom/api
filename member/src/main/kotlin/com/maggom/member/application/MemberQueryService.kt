package com.maggom.member.application

import com.maggom.member.port.`in`.MemberQueryUseCase
import com.maggom.member.port.out.MemberPort
import org.springframework.stereotype.Service

@Service
class MemberQueryService(
    private val memberPort: MemberPort,
) : MemberQueryUseCase {

    override fun existsByEmail(email: String): Boolean {
        return memberPort.existsByEmail(email)
    }
}
