package com.maggom.app.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@ConfigurationProperties(prefix = "maggom.cors")
class CorsProperties {
    var allowedOrigins: List<String> = emptyList()
}

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class CorsConfig(
    private val corsProperties: CorsProperties,
    @Value("\${maggom.mail.base-url}") private val apiBaseUrl: String,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        // 구독 해지 확인 페이지는 API 도메인에서 직접 서빙되고 폼도 같은 도메인으로 전송된다.
        // 프록시 뒤에서는 같은 출처가 교차 출처로 판정될 수 있어 API 자신의 출처를 허용한다.
        registry.addMapping(UNSUBSCRIBE_PATH)
            .allowedOrigins(apiBaseUrl.trimEnd('/'))
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600)

        registry.addMapping("/api/**")
            .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    companion object {
        private const val UNSUBSCRIBE_PATH = "/api/v1/subscriptions/unsubscribe"
    }
}
