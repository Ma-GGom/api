package com.maggom.member.adapter.out.persistence

import com.maggom.member.domain.Member
import com.maggom.member.domain.MemberRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "member")
class MemberJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: MemberRole = MemberRole.MEMBER,

    @Column(nullable = false)
    val isVerified: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): Member = Member(
        id = id,
        email = email,
        role = role,
        isVerified = isVerified,
        createdAt = createdAt,
    )

    companion object {
        fun from(member: Member): MemberJpaEntity = MemberJpaEntity(
            id = member.id,
            email = member.email,
            role = member.role,
            isVerified = member.isVerified,
            createdAt = member.createdAt,
        )
    }
}
