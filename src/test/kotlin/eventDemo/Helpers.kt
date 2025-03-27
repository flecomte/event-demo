package eventDemo

import eventDemo.business.entity.Card
import eventDemo.business.entity.Deck
import eventDemo.configuration.injection.appKoinModule
import eventDemo.configuration.ktor.configuration
import io.ktor.server.config.ApplicationConfig
import org.koin.core.Koin
import org.koin.core.module.KoinApplicationDslMarker
import org.koin.dsl.koinApplication

fun Deck.allCardCount(): Int =
  stack.size + discard.size + playersHands.values.flatten().size

fun Deck.allCards(): Set<Card> =
  stack + discard + playersHands.values.flatten()

@KoinApplicationDslMarker
suspend fun testApplicationWithConfig(block: suspend Koin.() -> Unit) {
  koinApplication { modules(appKoinModule(ApplicationConfig("application.conf").configuration())) }.koin.apply {
    block()
  }
}
