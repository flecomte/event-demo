package eventDemo.business.event.projection.gameList

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.projection.Projection
import kotlinx.serialization.Serializable

@Serializable
data class GameList(
  override val aggregateId: GameId,
  override val lastEventVersion: Int = 0,
  val status: Status = Status.OPENING,
  val players: Set<Player> = emptySet(),
  val winners: Set<Player> = emptySet(),
) : Projection<GameId> {
  enum class Status {
    OPENING,
    IS_STARTED,
    FINISH,
    CANCELED,
  }
}
