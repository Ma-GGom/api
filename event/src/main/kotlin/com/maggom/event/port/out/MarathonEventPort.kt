package com.maggom.event.port.out

import com.maggom.event.domain.MarathonEvent

interface MarathonEventPort {
    /** 지역·규모 조건에 맞는 접수중·접수예정 대회를 알림 노출 순서(NoticeOrder)로 돌려준다. */
    fun findOpenByRegions(regions: List<String>, includeSmall: Boolean = true): List<MarathonEvent>
}
