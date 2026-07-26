package eventDemo.contexts.game.domain.game.errors

import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.game.Card
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.domain.game.PlayerList
import eventDemo.contexts.game.domain.game.gameState.Deck
import eventDemo.contexts.game.domain.game.gameState.Game

abstract class GameException(
  message: String,
) : Exception(message)

abstract class IllegalActionException(
  message: String,
) : GameException(message)

class ItsNotTheTurnException(
  val playerId: Player.PlayerId,
) : IllegalActionException("It is not the turn of the player") {
  constructor(playerId: Player.PlayerId, event: GameEvent) : this(playerId)
}

class TheCardIsAColorCardException(
  val playerId: Player.PlayerId,
) : IllegalActionException("The card is a color card")

class TheCardHasNoColorException(
  val playerId: Player.PlayerId,
) : IllegalActionException("The card has no color, you must chose a color")

class ThePlayerHasRemainingCardsException(
  val player: Player,
) : IllegalActionException("The player has remaining cards")

class ThePlayerHasAlreadyWinException(
  val playerId: Player.PlayerId,
) : IllegalActionException("The player has already win")

class ThePlayerIsNotInTheGameException(
  val playerId: Player.PlayerId,
) : IllegalActionException("The player is not in the game")

class ThePlayerMustPlayACardException(
  val playerId: Player.PlayerId,
  val playableCards: Set<Card>,
) : IllegalActionException("The player must be play a card")

class NeedMorePlayersToStartGameException(
  val players: PlayerList,
) : IllegalActionException("You cannot start a game with less than 2 players!")

class AllPlayerNotReadyException(
  val players: PlayerList,
) : IllegalActionException("All players not ready!")

class DeckMissingCardsException(
  val players: PlayerList,
  deck: Deck,
) : IllegalActionException("The deck missing cards")

class InconsistentGameException(
  val game: Game,
) : GameException("Inconsistent game state")

class InconsistentEventVersionException(
  val game: Set<GameEvent>,
) : GameException("Inconsistent event version")
