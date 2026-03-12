package com.maggom.member.adapter.out.persistence

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return attribute?.joinToString(",") ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        if (dbData.isNullOrBlank()) return emptyList()

        return dbData.split(",")
    }
}
