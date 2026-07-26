package eventDemo.contexts.auth.infrastructure.configure

import io.ktor.server.application.Application

fun Application.configureAuth() {
  configureKtorAuth()
  configureAuthRoutes()
}
