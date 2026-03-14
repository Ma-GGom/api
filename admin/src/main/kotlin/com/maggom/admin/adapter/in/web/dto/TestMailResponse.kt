package com.maggom.admin.adapter.`in`.web.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TestMailResponse(
    val message: String,
    @JsonProperty("request_id") val requestId: String,
)
