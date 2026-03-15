package com.maggom.event.adapter.out.persistence

import com.maggom.event.domain.MarathonEventStatus
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MarathonEventQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val event = QMarathonEventJpaEntity.marathonEventJpaEntity

    fun findOpenByRegions(regions: List<String>): List<MarathonEventJpaEntity> {
        val now = LocalDateTime.now()

        val statusOrder = Expressions.cases()
            .`when`(event.status.eq(MarathonEventStatus.OPEN)).then(0)
            .otherwise(1)

        // OPEN: regEndDate 오름차순 (마감 임박 순), UPCOMING: regStartDate 오름차순 (오픈 빠른 순)
        val secondarySort = Expressions.cases()
            .`when`(event.status.eq(MarathonEventStatus.OPEN)).then(event.regEndDate)
            .otherwise(event.regStartDate)

        return queryFactory
            .selectFrom(event)
            .where(
                event.status.`in`(MarathonEventStatus.OPEN, MarathonEventStatus.UPCOMING),
                event.region.`in`(regions),
                event.regEndDate.isNull.or(event.regEndDate.gt(now)),
            )
            .orderBy(
                statusOrder.asc(),
                secondarySort.asc().nullsLast(),
            )
            .fetch()
    }
}
