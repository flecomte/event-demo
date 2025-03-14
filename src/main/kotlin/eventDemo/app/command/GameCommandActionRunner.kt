package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.app.command.command.ICantPlayCommand
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.command.command.IWantToPlayCardCommand
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.projection.GameStateRepository
import eventDemo.app.notification.Notification
import kotlinx.coroutines.channels.SendChannel

class GameCommandActionRunner(
  private val eventHandler: GameEventHandler,
  private val gameStateRepository: GameStateRepository,
) {
  suspend fun run(
    command: GameCommand,
    outgoingErrorChannelNotification: SendChannel<Notification>,
  ) {
    val gameState = gameStateRepository.getLast(command.payload.aggregateId)

    try {
      when (command) {
        is IWantToPlayCardCommand -> command.run(gameState, this.eventHandler)
        is IamReadyToPlayCommand -> command.run(gameState, this.eventHandler)
        is IWantToJoinTheGameCommand -> command.run(gameState, this.eventHandler)
        is ICantPlayCommand -> command.run(gameState, this.eventHandler)
      }
    } catch (e: CommandException) {
      errorNotifier(command, outgoingErrorChannelNotification)(e.message)
    }
  }
}
