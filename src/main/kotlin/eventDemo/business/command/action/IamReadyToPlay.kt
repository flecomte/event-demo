package eventDemo.business.command.action

import eventDemo.business.command.CommandException
import eventDemo.business.command.command.IamReadyToPlayCommand
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.projection.GameStateRepository

/**
 * A command to set as ready to play
 */
class IamReadyToPlay(
  private val gameStateRepository: GameStateRepository,
) : CommandAction<IamReadyToPlayCommand, PlayerReadyEvent> {
  @Throws(CommandException::class)
  override fun run(command: IamReadyToPlayCommand): (version: Int) -> PlayerReadyEvent {
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
