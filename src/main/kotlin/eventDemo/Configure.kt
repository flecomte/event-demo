package eventDemo

import eventDemo.app.actions.GameEventReactionSubscriber
import eventDemo.plugins.configureHttp
import eventDemo.plugins.configureHttpRouting
import eventDemo.plugins.configureKoin
import eventDemo.plugins.configureSecurity
import eventDemo.plugins.configureSerialization
import eventDemo.plugins.configureSockets
import eventDemo.plugins.configureWebSocketsGameRoute
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configure() {
    configureKoin()

    configureSecurity()

    configureSerialization()

    configureSockets()
    configureWebSocketsGameRoute(get(), get())

    configureHttp()
    configureHttpRouting()

    GameEventReactionSubscriber(get(), get())
        .init()
}
