package io.github.flecomte

import io.github.flecomte.plugins.configureHTTP
import io.github.flecomte.plugins.configureRouting
import io.github.flecomte.plugins.configureSecurity
import io.github.flecomte.plugins.configureSerialization
import io.github.flecomte.plugins.configureSockets
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureSockets()
    configureHTTP()
    configureRouting()
}
