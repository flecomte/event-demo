package eventDemo.contexts.game.application.command.handlers

import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.game.gameState.Game
import eventDemo.libs.eventSource.eventStore.VersionConflictException
import eventDemo.shared.command.Command
import eventDemo.shared.game.command.GameCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

sealed interface CommandHandler<C : Command> {
  fun handle(command: C)
}

abstract class GameEventManager(
  private val gameRepository: GameRepository,
  private val gameEventBus: GameEventBus,
) {
  private val logger = KotlinLogging.logger {}

  fun GameCommand.getGame(): Game =
    gameRepository.get(payload.aggregateId) ?: error("Game not found")

  fun GameEvent.getGame(): Game =
    gameRepository.get(aggregateId) ?: error("Game not found")

  @Throws(VersionConflictException::class)
  protected fun Game.saveEvents(): Game {
    gameRepository.save(this)
    return this
  }

  protected fun Game.publishEvents(): Game {
    gameEventBus.publish(recordedEvents)
    return this
  }

  protected inline fun <reified G : Game> Game.isStatusOrFail(message: String): G =
    this as? G ?: throw CommandException(message)

  protected fun <T> retry(
    mapAttempts: Int = 5,
    block: () -> T,
  ): T =
    try {
      block()
    } catch (e: VersionConflictException) {
      if (mapAttempts > 0) {
        logger.warn { "retry after version conflict (attempts left: $mapAttempts)" }
        retry(mapAttempts - 1, block)
      } else {
        logger.error { "Version conflict retry failed" }
        throw e
      }
    }
}
