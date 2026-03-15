package com.maggom.event.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface MarathonEventJpaRepository : JpaRepository<MarathonEventJpaEntity, Long>
