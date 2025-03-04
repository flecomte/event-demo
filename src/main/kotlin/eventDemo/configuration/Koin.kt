package eventDemo.configuration

import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventStream
import eventDemo.libs.event.EventBusInMemory
import eventDemo.libs.event.EventStreamInMemory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appKoinModule)
    }
}

val appKoinModule =
    module {
        single {
            GameEventStream(get(), EventStreamInMemory())
        }
        single {
            GameEventBus(EventBusInMemory())
        }
    }
