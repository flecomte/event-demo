package eventDemo.app.command.command

import eventDemo.app.command.CommandException
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.projection.GameState
import eventDemo.libs.command.CommandId
import kotlinx.serialization.Serializable

/**
 * A command to set as ready to play
 */
@Serializable
data class IamReadyToPlayCommand(
  override val payload: Payload,
) : GameCommand {
  override val id: CommandId = CommandId()

  @Serializable
  data class Payload(
    override val aggregateId: GameId,
    override val player: Player,
  ) : GameCommand.Payload

  @Throws(CommandException::class)
  suspend fun run(
    state: GameState,
    eventHandler: GameEventHandler,
  ) {
    val playerExist: Boolean = state.players.contains(payload.player)
    val playerIsAlreadyReady: Boolean = state.readyPlayers.contains(payload.player)

    if (state.isStarted) {
      throw CommandException("The game is already started")
    } else if (!playerExist) {
      throw CommandException("You are not in the game")
    } else if (playerIsAlreadyReady) {
      throw CommandException("You are already ready")
    } else {
      eventHandler.handle(payload.aggregateId) {
        PlayerReadyEvent(
          aggregateId = payload.aggregateId,
          player = payload.player,
          version = it,
        )
      }
    }
  }
}
