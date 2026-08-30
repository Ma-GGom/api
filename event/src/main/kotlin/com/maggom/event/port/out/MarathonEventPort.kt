package com.maggom.event.port.out

import com.maggom.event.domain.MarathonEvent

interface MarathonEventPort {
    fun findOpenByRegions(regions: List<String>, includeSmall: Boolean = true): List<MarathonEvent>
}
