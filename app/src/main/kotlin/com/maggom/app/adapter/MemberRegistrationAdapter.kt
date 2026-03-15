package com.maggom.app.adapter

import com.maggom.auth.port.out.MemberRegistrationPort
import com.maggom.member.port.`in`.RegisterMemberUseCase
import org.springframework.stereotype.Component

@Component
class MemberRegistrationAdapter(
    private val registerMemberUseCase: RegisterMemberUseCase,
) : MemberRegistrationPort {

    override fun register(email: String) {
        registerMemberUseCase.register(email)
    }
}
