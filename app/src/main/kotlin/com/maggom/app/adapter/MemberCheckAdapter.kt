package com.maggom.app.adapter

import com.maggom.auth.port.out.MemberCheckPort
import com.maggom.member.port.`in`.MemberQueryUseCase
import org.springframework.stereotype.Component

@Component
class MemberCheckAdapter(
    private val memberQueryUseCase: MemberQueryUseCase,
) : MemberCheckPort {

    override fun existsByEmail(email: String): Boolean {
        return memberQueryUseCase.existsByEmail(email)
    }
}
