package com.maggom.member.application

import com.maggom.common.exception.MemberNotFoundException
import com.maggom.member.port.`in`.SubscriptionCountUseCase
import com.maggom.member.port.`in`.SubscriptionDeleteUseCase
import com.maggom.member.port.`in`.SubscriptionQueryUseCase
import com.maggom.member.port.`in`.SubscriptionResult
import com.maggom.member.port.`in`.SubscriptionUpdateUseCase
import com.maggom.member.port.`in`.UpdateSubscriptionCommand
import com.maggom.member.port.out.MemberPort
import com.maggom.member.port.out.SubscriptionPreferencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionService(
    private val memberPort: MemberPort,
    private val subscriptionPreferencePort: SubscriptionPreferencePort,
) : SubscriptionQueryUseCase, SubscriptionUpdateUseCase, SubscriptionDeleteUseCase, SubscriptionCountUseCase {

    override fun getByEmail(email: String): SubscriptionResult {
        val member = memberPort.findByEmail(email) ?: throw MemberNotFoundException()
        val pref = subscriptionPreferencePort.findByMemberId(member.id)
            ?: throw MemberNotFoundException()

        return SubscriptionResult(
            receiveDays = pref.receiveDays,
            receiveTime = pref.receiveTime,
            prefRegions = pref.prefRegions,
            prefDistances = pref.prefDistances,
            includeSmall = pref.includeSmall,
        )
    }

    @Transactional
    override fun update(command: UpdateSubscriptionCommand) {
        val member = memberPort.findByEmail(command.email) ?: throw MemberNotFoundException()
        val pref = subscriptionPreferencePort.findByMemberId(member.id)
            ?: throw MemberNotFoundException()

        val updated = pref.copy(
            receiveDays = command.receiveDays ?: pref.receiveDays,
            receiveTime = command.receiveTime ?: pref.receiveTime,
            prefRegions = command.prefRegions ?: pref.prefRegions,
            prefDistances = command.prefDistances ?: pref.prefDistances,
            includeSmall = command.includeSmall ?: pref.includeSmall,
        )

        subscriptionPreferencePort.save(updated)
    }

    @Transactional
    override fun delete(email: String) {
        val member = memberPort.findByEmail(email) ?: throw MemberNotFoundException()
        subscriptionPreferencePort.deleteByMemberId(member.id)
        memberPort.deleteById(member.id)
    }

    override fun count(): Long {
        return memberPort.countAll()
    }
}
