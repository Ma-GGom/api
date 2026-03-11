package com.maggom.app.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule.Builder().build()
}
