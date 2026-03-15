package com.maggom.admin.adapter.`in`.web.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TestMailRequest(
    @JsonProperty("to_email") val toEmail: String,
    @JsonProperty("template_type") val templateType: MailTemplateType,
)

enum class MailTemplateType {
    WELCOME,
    NOTIFICATION,
}
