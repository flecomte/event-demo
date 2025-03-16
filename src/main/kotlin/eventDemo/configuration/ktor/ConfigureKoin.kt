package eventDemo.configuration.ktor

import eventDemo.configuration.injection.appKoinModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
  install(Koin) {
    slf4jLogger()
    modules(appKoinModule)
  }
}
