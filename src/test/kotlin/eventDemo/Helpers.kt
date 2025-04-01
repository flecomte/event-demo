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
import redis.clients.jedis.UnifiedJedis
import javax.sql.DataSource

fun Deck.allCardCount(): Int =
  stack.size + discard.size + playersHands.values.flatten().size

fun Deck.allCards(): Set<Card> =
  stack + discard + playersHands.values.flatten()

@KoinApplicationDslMarker
suspend fun <T> testKoinApplicationWithConfig(block: suspend Koin.() -> T): T =
  koinApplication { modules(appKoinModule(ApplicationConfig("application.conf").configuration())) }.koin.block()

@KtorDsl
suspend fun testApplicationWithConfig(block: suspend ApplicationTestBuilder.(koin: Koin) -> Unit) {
  testApplication {
    val conf = ApplicationConfig("application.conf")
    environment {
      config = conf
    }

    val koin = koinApplication { modules(appKoinModule(conf.configuration())) }.koin
    koin.cleanDataTest()
    koin.configureGameListener()
    block(koin)
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
  get<UnifiedJedis>().cleanProjections()
}
