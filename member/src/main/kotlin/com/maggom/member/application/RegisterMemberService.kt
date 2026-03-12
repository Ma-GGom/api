package com.maggom.member.application

import com.maggom.member.domain.Member
import com.maggom.member.domain.SubscriptionPreference
import com.maggom.member.port.`in`.RegisterMemberUseCase
import com.maggom.member.port.out.MemberPort
import com.maggom.member.port.out.SubscriptionPreferencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterMemberService(
    private val memberPort: MemberPort,
    private val subscriptionPreferencePort: SubscriptionPreferencePort,
) : RegisterMemberUseCase {

    @Transactional
    override fun register(email: String) {
        val member = memberPort.save(Member(email = email))
        subscriptionPreferencePort.save(SubscriptionPreference(memberId = member.id))
    }
}
