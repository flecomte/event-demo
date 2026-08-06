package eventDemo.contexts.game.infrastructure.configuration.ktor

import eventDemo.contexts.game.infrastructure.configuration.listener.configureProjectionListener
import eventDemo.contexts.game.infrastructure.configuration.listener.configureReactionListener
import io.ktor.server.application.Application
import org.koin.ktor.plugin.koin

fun Application.configureUno() {
  configureSerialization()

  configureWebSockets()
  declareWebSocketsRoute()

  configureHttpRouting()
  declareHttpGameRoute()

  koin().run {
    configureProjectionListener()
    configureReactionListener()
  }
}
