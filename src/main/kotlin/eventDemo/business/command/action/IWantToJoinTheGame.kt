package eventDemo.business.command.action

import eventDemo.business.command.CommandException
import eventDemo.business.command.command.IWantToJoinTheGameCommand
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.projection.GameStateRepository

/**
 * A command to perform an action to play a new card
 */
data class IWantToJoinTheGame(
  private val gameStateRepository: GameStateRepository,
) : CommandAction<IWantToJoinTheGameCommand, NewPlayerEvent> {
  override fun run(command: IWantToJoinTheGameCommand): (version: Int) -> NewPlayerEvent {
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
