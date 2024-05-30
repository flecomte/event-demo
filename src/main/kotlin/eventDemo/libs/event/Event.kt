package eventDemo.libs.event

import java.util.UUID

interface AggregateId {
    val id: UUID
}

interface Event<ID : AggregateId> {
    val id: ID
}
