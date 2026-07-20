package eventDemo.domain.command.action

import eventDemo.domain.command.CommandException
import eventDemo.domain.command.command.IWantToJoinTheGameCommand
import eventDemo.domain.event.event.NewPlayerEvent
import eventDemo.domain.event.projection.GameStateRepository

/**
 * A command to perform an action to play a new card
 */
data class IWantToJoinTheGame(
  private val gameStateRepository: GameStateRepository,
) : CommandAction<IWantToJoinTheGameCommand, NewPlayerEvent> {
  override fun run(command: IWantToJoinTheGameCommand): (version: Int) -> NewPlayerEvent {
    val state = gameStateRepository.get(command.payload.aggregateId)
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
