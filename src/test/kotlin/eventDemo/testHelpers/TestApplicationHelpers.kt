package eventDemo.testHelpers

import com.zaxxer.hikari.HikariDataSource
import eventDemo.configuration.appKoinModule
import eventDemo.configuration.configuration
import eventDemo.contexts.game.infrastructure.configuration.listener.configureProjectionListener
import eventDemo.contexts.game.infrastructure.configuration.listener.configureReactionListener
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.KtorDsl
import org.koin.core.Koin
import org.koin.core.module.KoinApplicationDslMarker
import org.koin.dsl.koinApplication
import org.koin.ktor.ext.getKoin

const val CONFIG_FILE_NAME = "application.conf"

@KoinApplicationDslMarker
suspend fun <T> testKoinApplicationWithConfig(block: suspend Koin.() -> T): T =
  koinApplication {
    modules(appKoinModule(ApplicationConfig(CONFIG_FILE_NAME).configuration))
  }.koin
    .run {
      cleanDataTest()
      configureProjectionListener()
      configureReactionListener()
      block()
        .apply { get<HikariDataSource>().close() }
    }

@KtorDsl
fun testApplicationWithConfig(
  configBuilder: Koin.() -> Unit = {},
  block: suspend ApplicationTestBuilder.() -> Unit,
) {
  val logger = KotlinLogging.logger {}
  testApplication {
    val conf = ApplicationConfig(CONFIG_FILE_NAME)
    environment {
      config = conf
    }

    application {
      logger.info { "Config App" }
      val koin = getKoin()
      koin.cleanDataTest()
      logger.info { "Starting A" }
      configBuilder(koin)
      logger.info { "A finish" }
    }
    logger.info { "Starting B" }
    this@testApplication.block()
    logger.info { "B finish" }
  }
}
