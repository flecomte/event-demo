package eventDemo.app.command.action

import eventDemo.app.command.CommandException
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.projection.GameStateRepository

/**
 * A command to perform an action to play a new card
 */
data class IWantToJoinTheGame(
  private val gameStateRepository: GameStateRepository,
) : CommandAction<IWantToJoinTheGameCommand, NewPlayerEvent> {
  override fun run(command: IWantToJoinTheGameCommand): (Int) -> NewPlayerEvent {
    val state = gameStateRepository.getLast(command.payload.aggregateId)
    if (!state.isStarted) {
      return {
        NewPlayerEvent(
          aggregateId = command.payload.aggregateId,
          player = command.payload.player,
          version = it,
        )
      }
    } else {
      throw CommandException("The game is already started")
    }
  }
}
