package eventDemo

import eventDemo.business.entity.Card
import eventDemo.business.entity.Deck
import eventDemo.configuration.business.configureGameListener
import eventDemo.configuration.injection.appKoinModule
import eventDemo.configuration.ktor.configuration
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.KtorDsl
import org.koin.core.Koin
import org.koin.core.module.KoinApplicationDslMarker
import org.koin.dsl.koinApplication
import org.koin.ktor.ext.getKoin
import redis.clients.jedis.UnifiedJedis
import javax.sql.DataSource

fun Deck.allCardCount(): Int =
  stack.size + discard.size + playersHands.values.flatten().size

fun Deck.allCards(): Set<Card> =
  stack + discard + playersHands.values.flatten()

@KoinApplicationDslMarker
suspend fun <T> testKoinApplicationWithConfig(block: suspend Koin.() -> T): T =
  koinApplication { modules(appKoinModule(ApplicationConfig("application.conf").configuration())) }
    .koin
    .apply {
      cleanDataTest()
      configureGameListener()
    }.block()

@KtorDsl
fun testApplicationWithConfig(
  configBuilder: Koin.() -> Unit = {},
  block: suspend ApplicationTestBuilder.() -> Unit,
) {
  testApplication {
    val conf = ApplicationConfig("application.conf")
    environment {
      config = conf
    }

    application {
      val koin = getKoin()
      koin.cleanDataTest()
      koin.configureGameListener()
      configBuilder(koin)
    }
    block()
  }
}

fun DataSource.cleanEventSource() {
  this.connection
    .prepareStatement(
      """
      truncate event_stream;
      """.trimIndent(),
    ).use {
      it.execute()
    }
}

fun UnifiedJedis.cleanProjections() {
  flushAll()
}

fun Koin.cleanDataTest() {
  get<DataSource>().cleanEventSource()
}
