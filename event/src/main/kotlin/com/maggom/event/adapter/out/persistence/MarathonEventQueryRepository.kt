package com.maggom.event.adapter.out.persistence

import com.maggom.event.domain.MarathonEventStatus
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MarathonEventQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val event = QMarathonEventJpaEntity.marathonEventJpaEntity

    fun findActiveByRegions(regions: List<String>): List<MarathonEventJpaEntity> {
        val now = LocalDateTime.now()

        return queryFactory
            .selectFrom(event)
            .where(
                event.status.`in`(MarathonEventStatus.OPEN, MarathonEventStatus.UPCOMING),
                event.region.`in`(regions),
                event.regEndDate.isNull.or(event.regEndDate.gt(now)),
            )
            .orderBy(event.regStartDate.asc())
            .fetch()
    }
}
