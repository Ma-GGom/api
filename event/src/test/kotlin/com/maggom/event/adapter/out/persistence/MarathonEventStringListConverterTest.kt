package com.maggom.event.adapter.out.persistence

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MarathonEventStringListConverterTest {

    private val converter = MarathonEventStringListConverter()

    @Test
    @DisplayName("크롤러가 적재한 중괄호 형식을 코스 목록으로 변환")
    fun converts_brace_wrapped_value() {
        assertEquals(listOf("5K", "10K"), converter.convertToEntityAttribute("{5K,10K}"))
        assertEquals(listOf("10K", "HALF"), converter.convertToEntityAttribute("{10K,HALF}"))
        assertEquals(listOf("FULL"), converter.convertToEntityAttribute("{FULL}"))
    }

    @Test
    @DisplayName("JSON 배열 형식도 코스 목록으로 변환")
    fun converts_json_array_value() {
        assertEquals(listOf("5K", "10K"), converter.convertToEntityAttribute("""["5K","10K"]"""))
    }

    @Test
    @DisplayName("구분자만 있는 일반 문자열도 그대로 변환")
    fun converts_plain_csv_value() {
        assertEquals(listOf("5K", "10K", "HALF"), converter.convertToEntityAttribute("5K,10K,HALF"))
    }

    @Test
    @DisplayName("비어 있는 값은 빈 목록으로 변환")
    fun converts_empty_value_to_empty_list() {
        assertEquals(emptyList(), converter.convertToEntityAttribute(null))
        assertEquals(emptyList(), converter.convertToEntityAttribute(""))
        assertEquals(emptyList(), converter.convertToEntityAttribute("{}"))
        assertEquals(emptyList(), converter.convertToEntityAttribute("[]"))
    }
}
