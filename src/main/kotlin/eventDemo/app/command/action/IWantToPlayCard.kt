package eventDemo.app.command.action

import eventDemo.app.command.CommandException
import eventDemo.app.command.command.IWantToPlayCardCommand
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.projection.GameStateRepository

/**
 * A command to perform an action to play a new card
 */
data class IWantToPlayCard(
  private val gameStateRepository: GameStateRepository,
) : CommandAction<IWantToPlayCardCommand, CardIsPlayedEvent> {
  override fun run(command: IWantToPlayCardCommand): (Int) -> CardIsPlayedEvent {
    val state = gameStateRepository.getLast(command.payload.aggregateId)

    if (!state.isStarted) {
      throw CommandException("The game is Not started")
    }
    if (state.currentPlayerTurn != command.payload.player) {
      throw CommandException("Its not your turn!")
    }
    if (!state.canBePlayThisCard(command.payload.player, command.payload.card)) {
      throw CommandException("You cannot play this card")
    }

    return { version ->
      CardIsPlayedEvent(
        aggregateId = command.payload.aggregateId,
        card = command.payload.card,
        player = command.payload.player,
        version = version,
      )
    }
  }
}
