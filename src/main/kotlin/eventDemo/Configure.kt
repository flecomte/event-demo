package eventDemo

import eventDemo.plugins.configureHTTP
import eventDemo.plugins.configureKoin
import eventDemo.plugins.configureRouting
import eventDemo.plugins.configureSecurity
import eventDemo.plugins.configureSerialization
import eventDemo.plugins.configureSockets
import io.ktor.server.application.Application

fun Application.configure() {
    configureSecurity()
    configureSerialization()
    configureSockets()
    configureHTTP()
    configureRouting()
    configureKoin()
    configureCommandHandler()
}
