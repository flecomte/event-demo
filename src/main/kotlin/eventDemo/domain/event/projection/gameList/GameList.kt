package eventDemo.domain.event.projection

import eventDemo.domain.entity.GameId
import eventDemo.domain.entity.Player
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
