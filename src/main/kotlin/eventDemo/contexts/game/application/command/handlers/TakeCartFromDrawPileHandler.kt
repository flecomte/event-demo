package eventDemo.contexts.game.application.command.handlers

import eventDemo.contexts.game.application.command.models.TakeCartFromDrawPileCommand
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.game.gameState.GameStarted

/**
 * A command to draw card on draw pile.
 *
 * Is can be triggered when you cannot play any card in your hand.
 */
class TakeCartFromDrawPileHandler(
  gameRepository: GameRepository,
  gameEventBus: GameEventBus,
) : GameEventManager(gameRepository, gameEventBus),
  CommandHandler<TakeCartFromDrawPileCommand> {
  override fun handle(command: TakeCartFromDrawPileCommand) {
    command
      .getGame()
      .isStatusOrFail<GameStarted>("The game is not started")
      .playerTakeCartFromDrawPile(command.payload.playerId, 1)
      .saveEvents()
      .publishEvents()
  }
}
