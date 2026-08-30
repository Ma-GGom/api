package com.maggom.app.migration

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 운영과 동일하게 Flyway로 스키마를 만들고 `ddl-auto: validate`로 엔티티 매핑을 검증한다.
 * 마이그레이션 스크립트와 엔티티가 어긋나면 컨텍스트 로딩 단계에서 실패한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class FlywayMigrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.flyway.enabled") { true }
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }
        }
    }

    @MockitoBean
    lateinit var mailSender: JavaMailSender

    @Autowired
    lateinit var entityManager: EntityManager

    @Test
    @DisplayName("마이그레이션이 성공하고 스키마 이력이 기록된다")
    fun migration_is_applied_and_recorded() {
        // when
        val applied = entityManager
            .createNativeQuery("SELECT COUNT(*) FROM flyway_schema_history WHERE success = true")
            .singleResult as Number

        // then
        assertTrue(applied.toInt() >= 1)
    }

    @Test
    @DisplayName("알림 정렬에 사용하는 컬럼이 마이그레이션에 포함된다")
    fun notification_sorting_columns_exist() {
        // when
        val count = entityManager
            .createNativeQuery(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_name = 'marathon_event' AND column_name IN ('event_scale', 'created_at')
                """.trimIndent()
            )
            .singleResult as Number

        // then
        assertEquals(2, count.toInt())
    }
}
