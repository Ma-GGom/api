package com.maggom.member.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<MemberJpaEntity, Long> {
    fun findByEmail(email: String): MemberJpaEntity?
    fun existsByEmail(email: String): Boolean
}
