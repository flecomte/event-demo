package eventDemo.contexts.game.infrastructure.persistence.projections.models

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import kotlinx.serialization.Serializable

/**
 * This [projection][Projection] is used to list all current games
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
