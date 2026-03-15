package com.maggom.member.adapter.out.persistence

import com.maggom.member.domain.SubscriptionPreference
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalTime

@Entity
@Table(name = "subscription_preference")
class SubscriptionPreferenceJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    val receiveDays: String = "MON,WED,FRI",

    @Column(nullable = false)
    val receiveTime: LocalTime = LocalTime.of(8, 0),

    @Convert(converter = StringListConverter::class)
    @Column(nullable = false)
    val prefRegions: List<String> = listOf("수도권"),

    @Convert(converter = StringListConverter::class)
    @Column(nullable = false)
    val prefDistances: List<String> = listOf("10K", "HALF"),

    @Column(nullable = false)
    val includeSmall: Boolean = true,
) {
    fun toDomain(): SubscriptionPreference = SubscriptionPreference(
        id = id,
        memberId = memberId,
        receiveDays = receiveDays,
        receiveTime = receiveTime,
        prefRegions = prefRegions,
        prefDistances = prefDistances,
        includeSmall = includeSmall,
    )

    companion object {
        fun from(pref: SubscriptionPreference): SubscriptionPreferenceJpaEntity = SubscriptionPreferenceJpaEntity(
            id = pref.id,
            memberId = pref.memberId,
            receiveDays = pref.receiveDays,
            receiveTime = pref.receiveTime,
            prefRegions = pref.prefRegions,
            prefDistances = pref.prefDistances,
            includeSmall = pref.includeSmall,
        )
    }
}
