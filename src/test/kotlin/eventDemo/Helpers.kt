package eventDemo

import eventDemo.business.entity.Card
import eventDemo.business.entity.Deck
import eventDemo.configuration.injection.appKoinModule
import eventDemo.configuration.ktor.configuration
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.KtorDsl
import org.koin.core.Koin
import org.koin.core.module.KoinApplicationDslMarker
import org.koin.dsl.koinApplication

fun Deck.allCardCount(): Int =
  stack.size + discard.size + playersHands.values.flatten().size

fun Deck.allCards(): Set<Card> =
  stack + discard + playersHands.values.flatten()

@KoinApplicationDslMarker
suspend fun testKoinApplicationWithConfig(block: suspend Koin.() -> Unit) {
  koinApplication { modules(appKoinModule(ApplicationConfig("application.conf").configuration())) }.koin.apply {
    block()
  }
}

@KtorDsl
suspend fun testApplicationWithConfig(block: suspend ApplicationTestBuilder.(koin: Koin) -> Unit) {
  testApplication {
    val conf = ApplicationConfig("application.conf")
    environment {
      config = conf
    }

    val koin = koinApplication { modules(appKoinModule(conf.configuration())) }.koin
    block(koin)
  }
}
