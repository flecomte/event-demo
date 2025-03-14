package eventDemo.app.event.projection

import eventDemo.libs.event.AggregateId

interface Projection<ID : AggregateId> {
  val aggregateId: ID
  val lastEventVersion: Int
}
