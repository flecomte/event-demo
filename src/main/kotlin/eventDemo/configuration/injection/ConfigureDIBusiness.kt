package eventDemo.configuration.injection

import eventDemo.business.command.GameCommandActionRunner
import eventDemo.business.command.GameCommandHandler
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.projection.projectionListener.PlayerNotificationListener
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf

fun Module.configureDIBusiness() {
  single {
    GameCommandHandler(get(), get(), get(), get())
  }
  singleOf(::GameEventHandler)
  singleOf(::GameCommandActionRunner)
  singleOf(::PlayerNotificationListener)
}
