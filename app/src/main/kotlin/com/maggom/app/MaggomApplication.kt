package com.maggom.app

import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.maggom"])
@AutoConfigurationPackage(basePackages = ["com.maggom"])
@EnableJpaRepositories(basePackages = ["com.maggom"])
class MaggomApplication

fun main(args: Array<String>) {
    runApplication<MaggomApplication>(*args)
}
