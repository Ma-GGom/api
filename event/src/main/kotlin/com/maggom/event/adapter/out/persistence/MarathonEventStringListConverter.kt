package com.maggom.event.adapter.out.persistence

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * 코스 목록 컬럼 변환기.
 *
 * 크롤러가 `{5K,10K}` 형태로 적재하므로 배열/JSON 표기의 감싸는 문자를 제거하고 읽는다.
 * 감싸는 문자를 남기면 첫·마지막 원소가 `{5K`, `10K}` 가 되어 구독 조건 매칭에서 누락된다.
 */
@Converter
class MarathonEventStringListConverter : AttributeConverter<List<String>, String> {

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return attribute?.joinToString(",") ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        if (dbData.isNullOrBlank()) return emptyList()

        val unwrapped = dbData.trim().removeSurrounding("{", "}").removeSurrounding("[", "]")

        return unwrapped.split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() }
    }
}
