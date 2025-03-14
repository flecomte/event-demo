package eventDemo.app.command.command

import eventDemo.app.command.ErrorNotifier
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.app.event.projection.GameState
import eventDemo.libs.command.CommandId
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
data class ICantPlayCommand(
  override val payload: Payload,
) : GameCommand {
  override val id: CommandId = CommandId()

  @Serializable
  data class Payload(
    override val aggregateId: GameId,
    override val player: Player,
  ) : GameCommand.Payload

  suspend fun run(
    state: GameState,
    playerErrorNotifier: ErrorNotifier,
    eventHandler: GameEventHandler,
  ) {
    if (state.currentPlayerTurn != payload.player) {
      playerErrorNotifier("Its not your turn!")
      return
    }
    val playableCards = state.playableCards(payload.player)
    if (playableCards.isEmpty()) {
      val takenCard = state.deck.stack.first()

      eventHandler.handle(payload.aggregateId) {
        PlayerHavePassEvent(
          aggregateId = payload.aggregateId,
          player = payload.player,
          takenCard = takenCard,
          version = it,
        )
      }
    } else {
      playerErrorNotifier("You can and must play one card, like ${playableCards.first()::class.simpleName}")
    }
  }
}
