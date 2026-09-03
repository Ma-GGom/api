package com.maggom.app.event

import com.maggom.event.adapter.out.persistence.MarathonEventJpaEntity
import com.maggom.event.adapter.out.persistence.MarathonEventQueryRepository
import com.maggom.event.domain.EventScale
import com.maggom.event.domain.MarathonEventStatus
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
    @DisplayName("OPEN 상태 + 지역 일치 + regEndDate 미래 - 반환")
    fun open_status_matching_region_future_reg_end_date_returns_event() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "서울특별시 마포구", regEndDate = now.plusDays(7))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(1, results.size)
    }

    @Test
    @DisplayName("UPCOMING 상태 + 지역 일치 - 반환")
    fun upcoming_status_matching_region_returns_event() {
        // given
        save(status = MarathonEventStatus.UPCOMING, region = "경기도 성남시", regEndDate = now.plusDays(7))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(1, results.size)
    }

    @Test
    @DisplayName("CLOSED 상태 - 제외")
    fun closed_status_excludes_event() {
        // given
        save(status = MarathonEventStatus.CLOSED, region = "서울특별시 강남구", regEndDate = now.plusDays(7))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertTrue(results.isEmpty())
    }

    @Test
    @DisplayName("지역 불일치 - 제외")
    fun mismatched_region_excludes_event() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "충청북도 청주시", regEndDate = now.plusDays(7))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertTrue(results.isEmpty())
    }

    @Test
    @DisplayName("regEndDate가 과거이면 제외")
    fun past_reg_end_date_excludes_event() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "서울특별시 마포구", regEndDate = now.minusDays(1))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertTrue(results.isEmpty())
    }

    @Test
    @DisplayName("regEndDate가 null이면 포함")
    fun null_reg_end_date_includes_event() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "인천광역시 남동구", regEndDate = null)

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(1, results.size)
    }

    @Test
    @DisplayName("여러 지역 중 하나라도 일치하면 반환")
    fun at_least_one_matching_region_returns_event() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "충청북도 청주시", regEndDate = now.plusDays(7))

        // when
        val results = repository.findOpenByRegions(listOf("수도권", "충청권"))

        // then
        assertEquals(1, results.size)
    }

    @Test
    @DisplayName("OPEN 이벤트는 regEndDate 오름차순 정렬")
    fun open_events_sorted_by_reg_end_date_ascending() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "서울특별시 마포구", regEndDate = now.plusDays(10))
        save(status = MarathonEventStatus.OPEN, region = "경기도 성남시", regEndDate = now.plusDays(3))
        save(status = MarathonEventStatus.OPEN, region = "인천광역시 연수구", regEndDate = now.plusDays(7))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(3, results.size)
        assertTrue(results[0].regEndDate!!.isBefore(results[1].regEndDate!!))
        assertTrue(results[1].regEndDate!!.isBefore(results[2].regEndDate!!))
    }

    @Test
    @DisplayName("접수 시작이 먼 UPCOMING은 OPEN보다 뒤로 정렬")
    fun distant_upcoming_events_sorted_after_open_events() {
        // given
        save(
            status = MarathonEventStatus.UPCOMING,
            region = "서울특별시 송파구",
            regStartDate = now.plusDays(30),
            regEndDate = now.plusDays(60),
        )
        save(status = MarathonEventStatus.OPEN, region = "서울특별시 마포구", regEndDate = now.plusDays(5))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(2, results.size)
        assertEquals(MarathonEventStatus.OPEN, results[0].status)
        assertEquals(MarathonEventStatus.UPCOMING, results[1].status)
    }

    @Test
    @DisplayName("includeSmall=false - SMALL 규모 대회 제외")
    fun exclude_small_scale_when_include_small_is_false() {
        // given
        save(
            status = MarathonEventStatus.OPEN,
            region = "서울특별시 마포구",
            regEndDate = now.plusDays(7),
            eventScale = EventScale.SMALL,
        )

        // when
        val results = repository.findOpenByRegions(listOf("수도권"), includeSmall = false)

        // then
        assertTrue(results.isEmpty())
    }

    @Test
    @DisplayName("includeSmall=false - MAJOR와 UNKNOWN 규모 대회는 포함")
    fun include_major_and_unknown_scale_when_include_small_is_false() {
        // given
        save(
            status = MarathonEventStatus.OPEN,
            region = "서울특별시 마포구",
            regEndDate = now.plusDays(7),
            eventScale = EventScale.MAJOR,
        )
        save(
            status = MarathonEventStatus.OPEN,
            region = "경기도 성남시",
            regEndDate = now.plusDays(7),
            eventScale = EventScale.UNKNOWN,
        )

        // when
        val results = repository.findOpenByRegions(listOf("수도권"), includeSmall = false)

        // then
        assertEquals(2, results.size)
        assertTrue(results.none { it.eventScale == EventScale.SMALL })
    }

    @Test
    @DisplayName("includeSmall=true - 모든 규모 대회 포함")
    fun include_all_scales_when_include_small_is_true() {
        // given
        save(
            status = MarathonEventStatus.OPEN,
            region = "서울특별시 마포구",
            regEndDate = now.plusDays(7),
            eventScale = EventScale.SMALL,
        )
        save(
            status = MarathonEventStatus.OPEN,
            region = "경기도 성남시",
            regEndDate = now.plusDays(7),
            eventScale = EventScale.MAJOR,
        )

        // when
        val results = repository.findOpenByRegions(listOf("수도권"), includeSmall = true)

        // then
        assertEquals(2, results.size)
    }

    @Test
    @DisplayName("접수 임박(7일 내 오픈) UPCOMING이 접수중보다 먼저 정렬")
    fun imminent_upcoming_events_sorted_first() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "서울특별시 마포구", regEndDate = now.plusDays(30))
        save(
            status = MarathonEventStatus.UPCOMING,
            region = "서울특별시 송파구",
            regStartDate = now.plusDays(3),
            regEndDate = now.plusDays(40),
        )

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(2, results.size)
        assertEquals(MarathonEventStatus.UPCOMING, results[0].status)
        assertEquals(MarathonEventStatus.OPEN, results[1].status)
    }

    @Test
    @DisplayName("마감 임박 대회가 일반 접수중 대회보다 먼저 정렬")
    fun closing_soon_events_sorted_before_other_open_events() {
        // given
        save(status = MarathonEventStatus.OPEN, region = "서울특별시 마포구", regEndDate = now.plusDays(30))
        save(status = MarathonEventStatus.OPEN, region = "경기도 성남시", regEndDate = now.plusDays(2))

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(2, results.size)
        assertEquals("경기도 성남시", results[0].region)
    }

    @Test
    @DisplayName("일반 접수중 대회는 최근 수집된 순으로 정렬")
    fun other_open_events_sorted_by_newest_crawled_first() {
        // given
        save(
            status = MarathonEventStatus.OPEN,
            region = "서울특별시 마포구",
            regEndDate = now.plusDays(30),
            createdAt = now.minusDays(10),
        )
        save(
            status = MarathonEventStatus.OPEN,
            region = "경기도 성남시",
            regEndDate = now.plusDays(60),
            createdAt = now.minusDays(1),
        )

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertEquals(2, results.size)
        assertEquals("경기도 성남시", results[0].region)
    }

    @Test
    @DisplayName("접수 종료일이 시작일보다 빠른 잘못된 데이터는 제외")
    fun events_with_inverted_registration_period_are_excluded() {
        // given
        save(
            status = MarathonEventStatus.OPEN,
            region = "서울특별시 마포구",
            regStartDate = now.plusDays(10),
            regEndDate = now.plusDays(5),
        )

        // when
        val results = repository.findOpenByRegions(listOf("수도권"))

        // then
        assertTrue(results.isEmpty())
    }

    private fun save(
        status: MarathonEventStatus,
        region: String,
        regStartDate: LocalDateTime = now.minusDays(1),
        regEndDate: LocalDateTime?,
        eventScale: EventScale = EventScale.UNKNOWN,
        createdAt: LocalDateTime = now,
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
            eventScale = eventScale,
            createdAt = createdAt,
            sourceName = "test",
            sourceUrl = "https://source.com",
            crawledAtKst = now,
        )
        entityManager.persist(entity)
        entityManager.flush()
    }
}
