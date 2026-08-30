package com.maggom.event.adapter.out.persistence

import com.maggom.event.domain.MarathonEvent
import com.maggom.event.port.out.MarathonEventPort
import org.springframework.stereotype.Component

@Component
class MarathonEventPersistenceAdapter(
    private val marathonEventQueryRepository: MarathonEventQueryRepository,
) : MarathonEventPort {

    override fun findOpenByRegions(regions: List<String>, includeSmall: Boolean): List<MarathonEvent> {
        return marathonEventQueryRepository
            .findOpenByRegions(regions, includeSmall)
            .map { it.toDomain() }
    }
}
