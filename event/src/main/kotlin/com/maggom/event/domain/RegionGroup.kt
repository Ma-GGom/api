package com.maggom.event.domain

object RegionGroup {

    private val PREFIXES: Map<String, List<String>> = mapOf(
        "수도권" to listOf("서울", "경기도", "인천"),
        "충청권" to listOf("충청북도", "충청남도", "대전", "세종"),
        "경상권" to listOf("경상북도", "경상남도", "대구", "부산", "울산"),
        "전라권" to listOf("전라북도", "전라남도", "전북특별자치도", "광주"),
        "강원권" to listOf("강원도", "강원특별자치도"),
        "제주권" to listOf("제주"),
    )

    fun toPrefixes(regionGroup: String): List<String> = PREFIXES[regionGroup] ?: listOf(regionGroup)
}
