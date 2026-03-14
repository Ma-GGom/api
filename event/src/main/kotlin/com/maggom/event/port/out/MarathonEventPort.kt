package com.maggom.event.port.out

import com.maggom.event.domain.MarathonEvent

interface MarathonEventPort {
    fun findActiveByRegions(regions: List<String>): List<MarathonEvent>
}
