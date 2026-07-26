package eventDemo.contexts.game.application.command.handlers

import eventDemo.contexts.auth.application.eventStores.UserRepository
import eventDemo.contexts.game.application.command.models.JoinTheGameCommand
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.game.gameState.GameCreated

/**
 * A command to perform an action to play a new card
 */
class JoinTheGameHandler(
  gameRepository: GameRepository,
  gameEventBus: GameEventBus,
  private val userRepository: UserRepository,
) : GameEventManager(gameRepository, gameEventBus),
  CommandHandler<JoinTheGameCommand> {
  override fun handle(command: JoinTheGameCommand) {
    val user = userRepository.get(command.userId) ?: error("User with id ${command.userId} doesn't exist")
    retry {
      command
        .getGame()
        .isStatusOrFail(GameCreated::class, "The game is started")
        .userJoinTheGame(
          userId = command.userId,
          name = user.username,
        ).saveEvents()
        .publishEvents()
    }
  }
}
