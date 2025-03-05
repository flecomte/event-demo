@file:Suppress("PropertyName")

@Suppress("ktlint:standard:property-naming")
val ktor_version: String by project
val kotlin_version: String by project
val kotlin_serialization_version: String by project
val logback_version: String by project
val koin_version: String by project
val kotlin_logging_version: String by project
val kotest_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "2.3.8"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

group = "io.github.flecomte"
version = "0.0.1"

application {
    mainClass.set("eventDemo.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.5.0")
    enableExperimentalRules.set(true)
}

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-auto-head-response-jvm")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-data-conversion")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlin_logging_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlin_serialization_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.11")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.mockk:mockk:1.13.17")
}
