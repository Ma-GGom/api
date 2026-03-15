package com.maggom.member.adapter.out.persistence

import com.maggom.member.domain.Member
import com.maggom.member.port.out.MemberPort
import org.springframework.stereotype.Component

@Component
class MemberPersistenceAdapter(
    private val memberJpaRepository: MemberJpaRepository,
) : MemberPort {

    override fun save(member: Member): Member {
        return memberJpaRepository.save(MemberJpaEntity.from(member)).toDomain()
    }

    override fun findByEmail(email: String): Member? {
        return memberJpaRepository.findByEmail(email)?.toDomain()
    }

    override fun existsByEmail(email: String): Boolean {
        return memberJpaRepository.existsByEmail(email)
    }

    override fun countAll(): Long {
        return memberJpaRepository.count()
    }

    override fun deleteById(id: Long) {
        memberJpaRepository.deleteById(id)
    }

    override fun findAll(): List<Member> {
        return memberJpaRepository.findAll().map { it.toDomain() }
    }
}
