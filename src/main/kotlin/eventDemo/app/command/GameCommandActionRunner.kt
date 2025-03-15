package eventDemo.app.command

import eventDemo.app.command.action.ICantPlay
import eventDemo.app.command.action.IWantToJoinTheGame
import eventDemo.app.command.action.IWantToPlayCard
import eventDemo.app.command.action.IamReadyToPlay
import eventDemo.app.command.command.GameCommand
import eventDemo.app.command.command.ICantPlayCommand
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.command.command.IWantToPlayCardCommand
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.event.event.GameEvent

class GameCommandActionRunner(
  private val iWantToPlayCard: IWantToPlayCard,
  private val iamReadyToPlay: IamReadyToPlay,
  private val iWantToJoinTheGame: IWantToJoinTheGame,
  private val iCantPlay: ICantPlay,
) {
  fun run(command: GameCommand): (Int) -> GameEvent =
    when (command) {
      is IWantToPlayCardCommand -> iWantToPlayCard.run(command)
      is IamReadyToPlayCommand -> iamReadyToPlay.run(command)
      is IWantToJoinTheGameCommand -> iWantToJoinTheGame.run(command)
      is ICantPlayCommand -> iCantPlay.run(command)
    }
}
