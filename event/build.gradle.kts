plugins {
    kotlin("plugin.jpa")
    kotlin("kapt")
}

val queryDslVersion = "7.0"

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:$queryDslVersion")
    implementation("io.github.openfeign.querydsl:querydsl-core:$queryDslVersion")
    kapt("io.github.openfeign.querydsl:querydsl-apt:$queryDslVersion:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
