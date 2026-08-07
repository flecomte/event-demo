package eventDemo.contexts.game.infrastructure.configuration.injections.application

import eventDemo.contexts.game.application.command.handlers.JoinTheGameHandler
import eventDemo.contexts.game.application.command.handlers.PlayCardHandler
import eventDemo.contexts.game.application.command.handlers.ReadyToPlayHandler
import eventDemo.contexts.game.application.command.handlers.TakeCartFromDrawPileHandler
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf

/**
 * Configure all actions
 */
fun Module.configureDICommandHandlers() {
  singleOf(::PlayCardHandler)
  singleOf(::ReadyToPlayHandler)
  singleOf(::JoinTheGameHandler)
  singleOf(::TakeCartFromDrawPileHandler)
}
