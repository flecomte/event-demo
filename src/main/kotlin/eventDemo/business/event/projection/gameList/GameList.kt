package eventDemo.business.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.libs.event.projection.Projection
import kotlinx.serialization.Serializable

/**
 * This [projection][Projection] is used to list all current games
 */
@Serializable
data class GameList(
  override val aggregateId: GameId,
  override val lastEventVersion: Int = 0,
  val status: Status = Status.OPENING,
  val players: Set<Player> = emptySet(),
  val winners: Set<Player> = emptySet(),
) : GameProjection {
  enum class Status {
    OPENING,
    IS_STARTED,
    FINISH,
    CANCELED,
  }
}
