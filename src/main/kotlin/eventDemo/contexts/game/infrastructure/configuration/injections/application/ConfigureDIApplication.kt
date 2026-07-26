package eventDemo.contexts.game.infrastructure.configuration.injections.application

import eventDemo.contexts.game.application.eventStores.GameEventStoreRepository
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.notification.EventToNotificationSubscriber
import eventDemo.contexts.game.application.reaction.ReactionListener
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Module.configureGameDIApplication() {
  configureDICommandHandlers()

  singleOf(::ReactionListener)
  singleOf(::EventToNotificationSubscriber)
  singleOf(::GameEventStoreRepository) bind GameRepository::class
}
