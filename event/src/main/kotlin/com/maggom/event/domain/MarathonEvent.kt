package com.maggom.event.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class MarathonEvent(
    val id: Long = 0,
    val title: String,
    val eventDate: LocalDate,
    val region: String,
    val distances: List<String>,
    val regStartDate: LocalDateTime,
    val regEndDate: LocalDateTime?,
    val linkUrl: String,
    val status: MarathonEventStatus,
    val eventScale: EventScale = EventScale.UNKNOWN,
    val sourceName: String,
    val sourceUrl: String,
    val crawledAtKst: LocalDateTime,
) {
    init {
        require(title.isNotBlank()) { "대회명은 필수입니다." }
        require(region.isNotBlank()) { "지역은 필수입니다." }
        require(distances.isNotEmpty()) { "코스는 하나 이상 있어야 합니다." }
        require(linkUrl.startsWith("http://") || linkUrl.startsWith("https://")) {
            "링크 URL은 http/https로 시작해야 합니다."
        }
        regEndDate?.let {
            require(!it.isBefore(regStartDate)) { "접수 종료일은 시작일보다 빠를 수 없습니다." }
        }
    }
}
