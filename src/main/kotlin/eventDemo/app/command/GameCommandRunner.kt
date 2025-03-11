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

class GameCommandRunner(
    private val eventHandler: GameEventHandler,
    private val gameStateRepository: GameStateRepository,
) {
    suspend fun run(
        command: GameCommand,
        outgoingErrorChannelNotification: SendChannel<Notification>,
    ) {
        val gameState = gameStateRepository.get(command.payload.gameId)
        val errorNotifier = errorNotifier(command, outgoingErrorChannelNotification)

        when (command) {
            is IWantToPlayCardCommand -> command.run(gameState, errorNotifier, this.eventHandler)
            is IamReadyToPlayCommand -> command.run(gameState, errorNotifier, this.eventHandler)
            is IWantToJoinTheGameCommand -> command.run(gameState, errorNotifier, this.eventHandler)
            is ICantPlayCommand -> command.run(gameState, errorNotifier, this.eventHandler)
        }
    }
}
