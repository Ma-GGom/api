package com.maggom.event.adapter.out.persistence

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.domain.MarathonEventStatus
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "marathon_event")
class MarathonEventJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val eventDate: LocalDate,

    @Column(nullable = false)
    val region: String,

    @Convert(converter = MarathonEventStringListConverter::class)
    @Column(nullable = false)
    val distances: List<String>,

    @Column(nullable = false)
    val regStartDate: LocalDateTime,

    val regEndDate: LocalDateTime?,

    @Column(nullable = false)
    val linkUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: MarathonEventStatus,

    @Column(nullable = false)
    val sourceName: String,

    @Column(nullable = false)
    val sourceUrl: String,

    @Column(nullable = false)
    val crawledAtKst: LocalDateTime,
) {
    fun toDomain(): MarathonEvent = MarathonEvent(
        id = id,
        title = title,
        eventDate = eventDate,
        region = region,
        distances = distances,
        regStartDate = regStartDate,
        regEndDate = regEndDate,
        linkUrl = linkUrl,
        status = status,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        crawledAtKst = crawledAtKst,
    )
}
