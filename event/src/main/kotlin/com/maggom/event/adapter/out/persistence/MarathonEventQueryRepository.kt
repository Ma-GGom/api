package com.maggom.event.adapter.out.persistence

import com.maggom.event.domain.EventScale
import com.maggom.event.domain.MarathonEventStatus
import com.maggom.event.domain.RegionGroup
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.DateTimeExpression
import com.querydsl.core.types.dsl.Expressions
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
                scalePredicate(includeSmall),
            )
            .orderBy(
                tier(now).asc(),
                earliestFirstKey(now).asc(),
                newestFirstKey(now).desc(),
                event.eventDate.asc(),
            )
            .fetch()
    }

    /**
     * 알림 우선순위.
     *
     * 0. 접수 임박  - 곧 접수가 열리는 대회 (인기 대회는 오픈 당일 마감되므로 가장 가치가 높다)
     * 1. 마감 임박  - 지금 신청하지 않으면 놓치는 대회
     * 2. 접수중     - 나머지 접수중 대회 (신규 수집순이라 발송할 때마다 목록이 갱신된다)
     * 3. 접수 예정  - 오픈까지 여유가 있는 대회
     */
    private fun tier(now: LocalDateTime) = Expressions.cases()
        .`when`(openingSoon(now)).then(0)
        .`when`(closingSoon(now)).then(1)
        .`when`(event.status.eq(MarathonEventStatus.OPEN)).then(2)
        .otherwise(3)

    /**
     * 0·1·3순위는 날짜가 빠른 순.
     * 2순위는 아래 신규순으로 정렬해야 하므로 고정값을 넣어 이 키의 영향을 없앤다.
     * (정렬은 tier로 이미 분리되어 다른 순위와 섞이지 않는다.)
     */
    private fun earliestFirstKey(now: LocalDateTime): DateTimeExpression<LocalDateTime> = Expressions.cases()
        .`when`(openingSoon(now)).then(event.regStartDate)
        .`when`(closingSoon(now)).then(event.regEndDate)
        .`when`(event.status.eq(MarathonEventStatus.OPEN)).then(sortKeyPlaceholder())
        .otherwise(event.regStartDate)

    /** 2순위(접수중)만 최근 수집된 대회를 먼저 노출해 매 발송마다 목록이 고이지 않게 한다. */
    private fun newestFirstKey(now: LocalDateTime): DateTimeExpression<LocalDateTime> = Expressions.cases()
        .`when`(openingSoon(now).or(closingSoon(now))).then(sortKeyPlaceholder())
        .`when`(event.status.eq(MarathonEventStatus.OPEN)).then(event.createdAt)
        .otherwise(sortKeyPlaceholder())

    private fun openingSoon(now: LocalDateTime): BooleanExpression {
        return event.status.eq(MarathonEventStatus.UPCOMING)
            .and(event.regStartDate.loe(now.plusDays(IMMINENT_DAYS)))
    }

    private fun closingSoon(now: LocalDateTime): BooleanExpression {
        return event.status.eq(MarathonEventStatus.OPEN)
            .and(event.regEndDate.isNotNull)
            .and(event.regEndDate.loe(now.plusDays(IMMINENT_DAYS)))
    }

    // includeSmall=false면 SMALL만 제외하고 MAJOR, UNKNOWN은 포함 (null 반환 시 조건 미적용)
    private fun scalePredicate(includeSmall: Boolean): BooleanExpression? {
        if (includeSmall) return null

        return event.eventScale.ne(EventScale.SMALL)
    }

    /** 해당 순위에서 쓰지 않는 정렬 키를 무력화하기 위한 고정값. */
    private fun sortKeyPlaceholder(): DateTimeExpression<LocalDateTime> {
        return Expressions.asDateTime(SORT_KEY_PLACEHOLDER)
    }

    companion object {
        private const val IMMINENT_DAYS = 7L
        private val SORT_KEY_PLACEHOLDER: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0)
    }
}
