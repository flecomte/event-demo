package eventDemo.app.command.action

import eventDemo.app.command.CommandException
import eventDemo.app.command.command.ICantPlayCommand
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.app.event.projection.GameStateRepository

/**
 * A command to perform an action to play a new card
 */
data class ICantPlay(
  private val gameStateRepository: GameStateRepository,
) : CommandAction<ICantPlayCommand, PlayerHavePassEvent> {
  override fun run(command: ICantPlayCommand): (Int) -> PlayerHavePassEvent {
    val state = gameStateRepository.getLast(command.payload.aggregateId)

    if (state.currentPlayerTurn != command.payload.player) {
      throw CommandException("Its not your turn!")
    }

    val playableCards = state.playableCards(command.payload.player)
    if (playableCards.isNotEmpty()) {
      throw CommandException("You can and must play one card, like ${playableCards.first()::class.simpleName}")
    }

    val takenCard = state.deck.stack.first()
    return { version ->
      PlayerHavePassEvent(
        aggregateId = command.payload.aggregateId,
        player = command.payload.player,
        takenCard = takenCard,
        version = version,
      )
    }
  }
}
