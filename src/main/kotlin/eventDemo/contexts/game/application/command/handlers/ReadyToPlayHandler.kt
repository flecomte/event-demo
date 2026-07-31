package eventDemo.contexts.game.application.command.handlers

import eventDemo.contexts.game.application.command.models.ReadyToPlayCommand
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.game.gameState.GameCreated

/**
 * A command to set as ready to play
 */
class ReadyToPlayHandler(
  gameRepository: GameRepository,
  gameEventBus: GameEventBus,
) : GameEventManager(gameRepository, gameEventBus),
  CommandHandler<ReadyToPlayCommand> {
  override fun handle(command: ReadyToPlayCommand) {
    command
      .getGame()
      .isStatusOrFail<GameCreated>("The game is started")
      .setReadyPlayer(command.payload.playerId)
      .saveEvents()
      .publishEvents()
  }
}
