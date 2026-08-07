package eventDemo.shared.game.projection

import eventDemo.shared.game.Player
import eventDemo.shared.ids.GameId
import kotlinx.serialization.Serializable

/**
 * This [projection][GameProjection] is used to list all current games
 */
@Serializable
data class GameList(
  val aggregateId: GameId,
  val status: Status = Status.OPENING,
  val players: Set<Player> = emptySet(),
  val winners: Set<Player.PlayerId> = emptySet(),
) : GameProjection {
  enum class Status {
    OPENING,
    IS_STARTED,
    FINISH,
    CANCELED,
  }
}
