package eventDemo.business.event.projection

import eventDemo.business.entity.GameId
import eventDemo.libs.event.projection.Projection
import kotlinx.serialization.Serializable

@Serializable
sealed interface GameProjection : Projection<GameId>
