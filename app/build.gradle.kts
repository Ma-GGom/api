plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))
    implementation(project(":member"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.postgresql:postgresql")
}
