package eventDemo.contexts.game.application.command.handlers

import eventDemo.contexts.game.application.command.models.PlayCardCommand
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.game.gameState.GameStarted

/**
 * A command to perform an action to play a new card
 */
class PlayCardHandler(
  gameRepository: GameRepository,
  gameEventBus: GameEventBus,
) : GameEventManager(gameRepository, gameEventBus),
  CommandHandler<PlayCardCommand> {
  override fun handle(command: PlayCardCommand) {
    command
      .getGame()
      .isStatusOrFail(GameStarted::class, "The game is not started")
      .playTheCard(
        card = command.payload.card,
        playerId = command.payload.playerId,
        chosenColor = command.payload.chosenColor,
      ).saveEvents()
      .publishEvents()
  }
}
