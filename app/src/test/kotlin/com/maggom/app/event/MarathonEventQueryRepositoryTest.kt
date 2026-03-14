package com.maggom.app.event

import com.maggom.event.adapter.out.persistence.MarathonEventJpaEntity
import com.maggom.event.adapter.out.persistence.MarathonEventQueryRepository
import com.maggom.event.domain.MarathonEventStatus
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@Testcontainers
class MarathonEventQueryRepositoryTest {

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
        }
    }

    @MockitoBean
    lateinit var mailSender: JavaMailSender

    @Autowired
    lateinit var repository: MarathonEventQueryRepository

    @Autowired
    lateinit var entityManager: EntityManager

    private val now: LocalDateTime = LocalDateTime.now()

    @Test
    fun `OPEN 상태 + 지역 일치 + regEndDate 미래 - 반환`() {
        save(status = MarathonEventStatus.OPEN, region = "수도권", regEndDate = now.plusDays(7))

        val results = repository.findActiveByRegions(listOf("수도권"))

        assertEquals(1, results.size)
    }

    @Test
    fun `UPCOMING 상태 + 지역 일치 - 반환`() {
        save(status = MarathonEventStatus.UPCOMING, region = "수도권", regEndDate = now.plusDays(7))

        val results = repository.findActiveByRegions(listOf("수도권"))

        assertEquals(1, results.size)
    }

    @Test
    fun `CLOSED 상태 - 제외`() {
        save(status = MarathonEventStatus.CLOSED, region = "수도권", regEndDate = now.plusDays(7))

        val results = repository.findActiveByRegions(listOf("수도권"))

        assertTrue(results.isEmpty())
    }

    @Test
    fun `지역 불일치 - 제외`() {
        save(status = MarathonEventStatus.OPEN, region = "충청권", regEndDate = now.plusDays(7))

        val results = repository.findActiveByRegions(listOf("수도권"))

        assertTrue(results.isEmpty())
    }

    @Test
    fun `regEndDate가 과거이면 제외`() {
        save(status = MarathonEventStatus.OPEN, region = "수도권", regEndDate = now.minusDays(1))

        val results = repository.findActiveByRegions(listOf("수도권"))

        assertTrue(results.isEmpty())
    }

    @Test
    fun `regEndDate가 null이면 포함`() {
        save(status = MarathonEventStatus.OPEN, region = "수도권", regEndDate = null)

        val results = repository.findActiveByRegions(listOf("수도권"))

        assertEquals(1, results.size)
    }

    @Test
    fun `여러 지역 중 하나라도 일치하면 반환`() {
        save(status = MarathonEventStatus.OPEN, region = "충청권", regEndDate = now.plusDays(7))

        val results = repository.findActiveByRegions(listOf("수도권", "충청권"))

        assertEquals(1, results.size)
    }

    @Test
    fun `regStartDate 오름차순 정렬`() {
        save(status = MarathonEventStatus.OPEN, region = "수도권", regStartDate = now.plusDays(5), regEndDate = now.plusDays(10))
        save(status = MarathonEventStatus.OPEN, region = "수도권", regStartDate = now.plusDays(1), regEndDate = now.plusDays(10))
        save(status = MarathonEventStatus.OPEN, region = "수도권", regStartDate = now.plusDays(3), regEndDate = now.plusDays(10))

        val results = repository.findActiveByRegions(listOf("수도권"))

        assertEquals(3, results.size)
        assertTrue(results[0].regStartDate.isBefore(results[1].regStartDate))
        assertTrue(results[1].regStartDate.isBefore(results[2].regStartDate))
    }

    private fun save(
        status: MarathonEventStatus,
        region: String,
        regStartDate: LocalDateTime = now.minusDays(1),
        regEndDate: LocalDateTime?,
    ) {
        val entity = MarathonEventJpaEntity(
            title = "테스트 마라톤",
            eventDate = LocalDate.now().plusMonths(1),
            region = region,
            distances = listOf("10K", "HALF"),
            regStartDate = regStartDate,
            regEndDate = regEndDate,
            linkUrl = "https://example.com",
            status = status,
            sourceName = "test",
            sourceUrl = "https://source.com",
            crawledAtKst = now,
        )
        entityManager.persist(entity)
        entityManager.flush()
    }
}
