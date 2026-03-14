package com.maggom.member.port.out

import com.maggom.member.domain.Member

interface MemberPort {
    fun save(member: Member): Member
    fun findByEmail(email: String): Member?
    fun existsByEmail(email: String): Boolean
    fun countAll(): Long
    fun deleteById(id: Long)
    fun findAll(): List<Member>
}
