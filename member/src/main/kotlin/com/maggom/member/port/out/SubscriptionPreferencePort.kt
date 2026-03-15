package com.maggom.member.port.out

import com.maggom.member.domain.SubscriptionPreference

interface SubscriptionPreferencePort {
    fun save(subscriptionPreference: SubscriptionPreference): SubscriptionPreference
    fun findByMemberId(memberId: Long): SubscriptionPreference?
    fun deleteByMemberId(memberId: Long)
}
