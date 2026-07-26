package eventDemo.configuration

import eventDemo.contexts.auth.infrastructure.configure.configureAuth
import eventDemo.contexts.game.infrastructure.configuration.ktor.configureUno
import io.ktor.server.application.Application

fun Application.configure() {
  configureKoin()
  configureAuth()
  configureUno()
}
