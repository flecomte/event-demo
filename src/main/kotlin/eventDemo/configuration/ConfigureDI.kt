package eventDemo.configuration

import eventDemo.app.command.GameCommandHandler
import eventDemo.app.command.command.GameCommand
import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.projection.GameStateRepository
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import eventDemo.libs.command.CommandStreamChannelBuilder
import eventDemo.libs.event.EventBusInMemory
import eventDemo.libs.event.EventStreamInMemory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.dsl.singleOf
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
            GameEventBus(EventBusInMemory())
        }
        single {
            GameEventStream(EventStreamInMemory())
        }
        single {
            GameStateRepository(get(), get())
        }
        single {
            CommandStreamChannelBuilder<GameCommand>()
        }

        singleOf(::GameEventHandler)
        singleOf(::GameCommandHandler)
        singleOf(::GameEventPlayerNotificationListener)
    }
