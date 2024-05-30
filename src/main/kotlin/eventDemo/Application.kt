package eventDemo

import eventDemo.plugins.configureCommandHandler
import eventDemo.plugins.configureHTTP
import eventDemo.plugins.configureKoin
import eventDemo.plugins.configureRouting
import eventDemo.plugins.configureSecurity
import eventDemo.plugins.configureSerialization
import eventDemo.plugins.configureSockets
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module, watchPaths = listOf("classes"))
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureSockets()
    configureHTTP()
    configureRouting()
    configureKoin()
    configureCommandHandler()
}
