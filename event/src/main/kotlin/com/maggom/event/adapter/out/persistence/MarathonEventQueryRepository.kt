package com.maggom.event.adapter.out.persistence

import com.maggom.event.domain.EventScale
import com.maggom.event.domain.MarathonEventStatus
import com.maggom.event.domain.RegionGroup
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MarathonEventQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val event = QMarathonEventJpaEntity.marathonEventJpaEntity

    fun findOpenByRegions(regions: List<String>, includeSmall: Boolean = true): List<MarathonEventJpaEntity> {
        val now = LocalDateTime.now()

        val prefixes = regions.flatMap { RegionGroup.toPrefixes(it) }
        val regionPredicate = prefixes.fold(BooleanBuilder()) { builder, prefix ->
            builder.or(event.region.startsWith(prefix))
        }

        return queryFactory
            .selectFrom(event)
            .where(
                event.status.`in`(MarathonEventStatus.OPEN, MarathonEventStatus.UPCOMING),
                regionPredicate,
                event.regEndDate.isNull.or(event.regEndDate.gt(now)),
                // 수집 오류로 접수 기간이 뒤집힌 행은 도메인 불변식을 깨므로 조회 단계에서 제외한다.
                event.regEndDate.isNull.or(event.regEndDate.goe(event.regStartDate)),
                scalePredicate(includeSmall),
            )
            // 노출 순서는 발송 시점 날짜로 판단하는 도메인 규칙(NoticeOrder)이 정한다.
            .orderBy(event.eventDate.asc(), event.id.asc())
            .fetch()
    }

    // includeSmall=false면 SMALL만 제외하고 MAJOR, UNKNOWN은 포함 (null 반환 시 조건 미적용)
    private fun scalePredicate(includeSmall: Boolean): BooleanExpression? {
        if (includeSmall) return null

        return event.eventScale.ne(EventScale.SMALL)
    }
}
