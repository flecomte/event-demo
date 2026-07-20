package eventDemo.domain.command

import eventDemo.domain.command.action.ICantPlay
import eventDemo.domain.command.action.IWantToJoinTheGame
import eventDemo.domain.command.action.IWantToPlayCard
import eventDemo.domain.command.action.IamReadyToPlay
import eventDemo.domain.command.command.GameCommand
import eventDemo.domain.command.command.ICantPlayCommand
import eventDemo.domain.command.command.IWantToJoinTheGameCommand
import eventDemo.domain.command.command.IWantToPlayCardCommand
import eventDemo.domain.command.command.IamReadyToPlayCommand
import eventDemo.domain.event.event.GameEvent

class GameCommandActionRunner(
  private val iWantToPlayCard: IWantToPlayCard,
  private val iamReadyToPlay: IamReadyToPlay,
  private val iWantToJoinTheGame: IWantToJoinTheGame,
  private val iCantPlay: ICantPlay,
) {
  fun run(command: GameCommand): (version: Int) -> GameEvent =
    when (command) {
      is IWantToPlayCardCommand -> iWantToPlayCard.run(command)
      is IamReadyToPlayCommand -> iamReadyToPlay.run(command)
      is IWantToJoinTheGameCommand -> iWantToJoinTheGame.run(command)
      is ICantPlayCommand -> iCantPlay.run(command)
    }
}
