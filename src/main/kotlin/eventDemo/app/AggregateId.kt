package eventDemo.app

import java.util.UUID

sealed interface AggregateId {
    val id: UUID
}

@JvmInline
value class GameId(override val id: UUID = UUID.randomUUID()) : AggregateId {
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString(): String = id.toString()
}
