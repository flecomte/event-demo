package eventDemo.contexts.auth.infrastructure.configure

import eventDemo.configuration.configuration
import eventDemo.contexts.auth.application.eventStores.UserEventStoreRepository
import eventDemo.contexts.auth.application.ports.UserProjectionRepository
import eventDemo.contexts.auth.infrastructure.rest.createUserRoute
import eventDemo.contexts.auth.infrastructure.rest.loginRoute
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get

fun Application.configureAuthRoutes() {
  val userRepository = get<UserEventStoreRepository>()
  val userProjectionRepository = get<UserProjectionRepository>()
  routing {
    createUserRoute(userRepository)
    loginRoute(environment.config.configuration.jwtSecret, userProjectionRepository)
  }
}
