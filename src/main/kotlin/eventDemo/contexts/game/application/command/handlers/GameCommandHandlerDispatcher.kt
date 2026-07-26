package eventDemo.contexts.game.application.command.handlers

import eventDemo.contexts.game.application.command.models.GameCommand
import eventDemo.contexts.game.application.command.models.JoinTheGameCommand
import eventDemo.contexts.game.application.command.models.PlayCardCommand
import eventDemo.contexts.game.application.command.models.ReadyToPlayCommand
import eventDemo.contexts.game.application.command.models.TakeCartFromDrawPileCommand
import eventDemo.contexts.game.domain.game.GameId
import java.util.Collections

class GameCommandHandlerDispatcher(
  private val playCardHandler: PlayCardHandler,
  private val readyToPlayHandler: ReadyToPlayHandler,
  private val joinTheGameHandler: JoinTheGameHandler,
  private val takeCartFromDrawPileHandler: TakeCartFromDrawPileHandler,
) {
  companion object {
    val lock: MutableMap<GameId, String> = Collections.synchronizedMap(mutableMapOf())
  }

  fun dispatch(command: GameCommand) {
    synchronized(lock.getOrPut(command.payload.aggregateId) { command.payload.aggregateId.toString() }) {
      when (command) {
        is JoinTheGameCommand -> joinTheGameHandler.handle(command)
        is ReadyToPlayCommand -> readyToPlayHandler.handle(command)
        is PlayCardCommand -> playCardHandler.handle(command)
        is TakeCartFromDrawPileCommand -> takeCartFromDrawPileHandler.handle(command)
      }
    }
  }
}
