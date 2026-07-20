package eventDemo.domain.event.projection

import eventDemo.domain.entity.GameId
import eventDemo.libs.event.projection.Projection
import kotlinx.serialization.Serializable

@Serializable
sealed interface GameProjection : Projection<GameId>
