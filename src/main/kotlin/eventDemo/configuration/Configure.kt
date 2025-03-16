package eventDemo.configuration

import eventDemo.configuration.business.configureGameListener
import eventDemo.configuration.ktor.configureHttpRouting
import eventDemo.configuration.ktor.configureKoin
import eventDemo.configuration.ktor.configureSecurity
import eventDemo.configuration.ktor.configureSerialization
import eventDemo.configuration.ktor.configureWebSockets
import eventDemo.configuration.route.declareHttpGameRoute
import eventDemo.configuration.route.declareWebSocketsGameRoute
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configure() {
  configureKoin()

  configureSecurity()

  configureSerialization()

  configureWebSockets()
  declareWebSocketsGameRoute(get(), get())

  configureHttpRouting()
  declareHttpGameRoute()

  configureGameListener()
}
