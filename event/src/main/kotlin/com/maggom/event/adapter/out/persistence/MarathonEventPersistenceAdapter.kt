package com.maggom.event.adapter.out.persistence

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.port.out.MarathonEventPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MarathonEventPersistenceAdapter(
    private val marathonEventQueryRepository: MarathonEventQueryRepository,
) : MarathonEventPort {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun findOpenByRegions(regions: List<String>, includeSmall: Boolean): List<MarathonEvent> {
        return marathonEventQueryRepository
            .findOpenByRegions(regions, includeSmall)
            .mapNotNull { toDomainOrNull(it) }
    }

    /** 수집 데이터가 도메인 불변식을 어겨도 해당 대회만 건너뛰고 알림 발송은 계속한다. */
    private fun toDomainOrNull(entity: MarathonEventJpaEntity): MarathonEvent? {
        return try {
            entity.toDomain()
        } catch (e: IllegalArgumentException) {
            log.warn("잘못된 대회 데이터를 건너뜁니다 - id: {}, 사유: {}", entity.id, e.message)

            null
        }
    }
}
