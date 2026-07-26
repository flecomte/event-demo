package eventDemo.contexts.game.domain.game.gameState

import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.domain.game.PlayerList

data class GameEnded(
  override val aggregateId: GameId,
  override val players: PlayerList,
  val playerWins: Set<Player.PlayerId> = emptySet(),
  override val version: Int,
  override val recordedEvents: Set<GameEvent>,
) : Game {
  init {
    if (!players.map { it.id }.containsAll(playerWins)) {
      throw IllegalArgumentException("Player ${players.map { it.id }} were not in players")
    }
  }
}
