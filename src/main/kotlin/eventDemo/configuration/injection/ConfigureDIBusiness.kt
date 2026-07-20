package eventDemo.configuration.injection

import eventDemo.domain.command.GameCommandActionRunner
import eventDemo.domain.command.GameCommandHandler
import eventDemo.domain.event.GameEventHandler
import eventDemo.domain.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.domain.event.projection.projectionListener.ReactionListener
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf

fun Module.configureDIBusiness() {
  single {
    GameCommandHandler(get(), get(), get(), get())
  }
  singleOf(::GameEventHandler)
  singleOf(::GameCommandActionRunner)
  singleOf(::PlayerNotificationListener)
  singleOf(::ReactionListener)
}
