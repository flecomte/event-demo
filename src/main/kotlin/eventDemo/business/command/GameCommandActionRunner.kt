package eventDemo.business.command

import eventDemo.business.command.action.ICantPlay
import eventDemo.business.command.action.IWantToJoinTheGame
import eventDemo.business.command.action.IWantToPlayCard
import eventDemo.business.command.action.IamReadyToPlay
import eventDemo.business.command.command.GameCommand
import eventDemo.business.command.command.ICantPlayCommand
import eventDemo.business.command.command.IWantToJoinTheGameCommand
import eventDemo.business.command.command.IWantToPlayCardCommand
import eventDemo.business.command.command.IamReadyToPlayCommand
import eventDemo.business.event.event.GameEvent

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
