package eventDemo.app.command.action

import eventDemo.app.command.CommandException
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.projection.GameStateRepository

/**
 * A command to set as ready to play
 */
class IamReadyToPlay(
  private val gameStateRepository: GameStateRepository,
) : CommandAction<IamReadyToPlayCommand, PlayerReadyEvent> {
  @Throws(CommandException::class)
  override fun run(command: IamReadyToPlayCommand): (Int) -> PlayerReadyEvent {
    val state = gameStateRepository.getLast(command.payload.aggregateId)
    val playerExist: Boolean = state.players.contains(command.payload.player)
    val playerIsAlreadyReady: Boolean = state.readyPlayers.contains(command.payload.player)

    if (state.isStarted) {
      throw CommandException("The game is already started")
    } else if (!playerExist) {
      throw CommandException("You are not in the game")
    } else if (playerIsAlreadyReady) {
      throw CommandException("You are already ready")
    } else {
      return { version: Int ->
        PlayerReadyEvent(
          aggregateId = command.payload.aggregateId,
          player = command.payload.player,
          version = version,
        )
      }
    }
  }
}
