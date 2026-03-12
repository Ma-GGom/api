package com.maggom.member.adapter.out.persistence

import com.maggom.member.domain.SubscriptionPreference
import com.maggom.member.port.out.SubscriptionPreferencePort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SubscriptionPreferencePersistenceAdapter(
    private val subscriptionPreferenceJpaRepository: SubscriptionPreferenceJpaRepository,
) : SubscriptionPreferencePort {

    override fun save(subscriptionPreference: SubscriptionPreference): SubscriptionPreference {
        return subscriptionPreferenceJpaRepository.save(
            SubscriptionPreferenceJpaEntity.from(subscriptionPreference)
        ).toDomain()
    }

    override fun findByMemberId(memberId: Long): SubscriptionPreference? {
        return subscriptionPreferenceJpaRepository.findByMemberId(memberId)?.toDomain()
    }

    @Transactional
    override fun deleteByMemberId(memberId: Long) {
        subscriptionPreferenceJpaRepository.deleteByMemberId(memberId)
    }
}
