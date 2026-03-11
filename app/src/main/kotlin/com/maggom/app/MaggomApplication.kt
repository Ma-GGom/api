package com.maggom.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.maggom"])
class MaggomApplication

fun main(args: Array<String>) {
    runApplication<MaggomApplication>(*args)
}
