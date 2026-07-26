import org.jlleitschuh.gradle.ktlint.KtlintExtension

val ktorVersion: Provider<String> = providers.gradleProperty("ktor_version")
val kotlinVersion: Provider<String> = providers.gradleProperty("kotlin_version")
val kotlinSerializationVersion: Provider<String> = providers.gradleProperty("kotlin_serialization_version")
val logbackVersion: Provider<String> = providers.gradleProperty("logback_version")
val koinVersion: Provider<String> = providers.gradleProperty("koin_version")
val kotlinLoggingVersion: Provider<String> = providers.gradleProperty("kotlin_logging_version")
val kotestVersion: Provider<String> = providers.gradleProperty("kotest_version")

plugins {
  application
  kotlin("jvm") version "2.1.21"
  id("io.ktor.plugin") version "3.5.1"
  id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10"
  id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
  id("com.avast.gradle.docker-compose") version "0.17.12"
}

group = "io.github.flecomte"

application {
  mainClass.set("eventDemo.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

configure<KtlintExtension> {
  version.set("1.8.0")
}
ktlint {
  reporters {
    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
  }
}

repositories {
  mavenCentral()
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

dockerCompose {
  val composeFile =
    if (project.hasProperty("ci")) {
      // Use docker-compose-ci.yaml for the CI
      "docker/docker-compose-ci.yaml"
    } else {
      // Use docker-compose-test.yaml for local tests
      "docker/docker-compose-test.yaml"
    }
  useComposeFiles.set(listOf(composeFile))
  setProjectName("event-demo-test")
}

tasks.test {
  dependsOn("composeUp")
  dockerCompose.useComposeFiles.set(listOf("docker/docker-compose-test.yaml"))
  dockerCompose.setProjectName("event-demo-test")
}

tasks.named("run") {
  dependsOn("composeUp")
  dockerCompose.useComposeFiles.set(listOf("docker/docker-compose-test.yaml"))
  dockerCompose.setProjectName("event-demo-dev")
}

tasks.register<Copy>("copyEnv") {
  group = "docker"
  description = "copy the default dotenv file"
  from("docker")
  into("docker")
  rename {
    it.removeSuffix(".template")
  }
  include(".env.template")
  eachFile {
    if (File("docker/$name").exists()) {
      exclude()
    }
  }
  doLast {
    val files =
      listOf(
        File("docker/pgadmin.secret"),
        File("docker/postgresql.secret"),
      )

    files.forEach {
      if (!it.exists()) {
        it.writeText("changeit")
      }
    }
  }
}
tasks.composeUp {
  dependsOn("copyEnv")
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
  implementation("io.ktor:ktor-client-auth")
  implementation("ch.qos.logback:logback-classic:${logbackVersion.get()}")
  implementation("io.insert-koin:koin-ktor:${koinVersion.get()}")
  implementation("io.insert-koin:koin-logger-slf4j:${koinVersion.get()}")
  implementation("io.github.oshai:kotlin-logging-jvm:${kotlinLoggingVersion.get()}")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${kotlinSerializationVersion.get()}")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
  implementation("redis.clients:jedis:5.2.0")
  implementation("org.postgresql:postgresql:42.7.13")
  implementation("com.zaxxer:HikariCP:6.3.0")
  implementation("com.rabbitmq:amqp-client:5.25.0")
  implementation("com.password4j:password4j:1.8.4")

  // Force version of sub library (for security)
  implementation("commons-codec:commons-codec:1.13")

  testImplementation("io.kotest:kotest-extensions-koin:${kotestVersion.get()}")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${kotlinVersion.get()}")
  testImplementation("io.ktor:ktor-server-test-host-jvm:${ktorVersion.get()}")
  testImplementation("io.kotest:kotest-runner-junit5:${kotestVersion.get()}")
  testImplementation("io.mockk:mockk:1.13.17")
  testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}
