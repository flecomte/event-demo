package eventDemo.contexts.game.application.command.handlers

import eventDemo.shared.game.command.GameCommand
import eventDemo.shared.game.command.JoinTheGameCommand
import eventDemo.shared.game.command.PlayCardCommand
import eventDemo.shared.game.command.ReadyToPlayCommand
import eventDemo.shared.game.command.TakeCartFromDrawPileCommand
import eventDemo.shared.ids.GameId
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
