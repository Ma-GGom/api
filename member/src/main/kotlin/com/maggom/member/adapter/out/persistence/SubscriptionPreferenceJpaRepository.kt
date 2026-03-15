package com.maggom.member.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionPreferenceJpaRepository : JpaRepository<SubscriptionPreferenceJpaEntity, Long> {
    fun findByMemberId(memberId: Long): SubscriptionPreferenceJpaEntity?
    fun deleteByMemberId(memberId: Long)
}
