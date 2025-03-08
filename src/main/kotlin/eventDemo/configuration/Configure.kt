package eventDemo.configuration

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
