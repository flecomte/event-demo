package eventDemo.libs.event

import javax.sql.DataSource

class EventStoreInPostgresql<E : Event<ID>, ID : AggregateId>(
  private val dataSource: DataSource,
  private val objectToString: (E) -> String,
  private val stringToObject: (String) -> E,
) : EventStore<E, ID> {
  override fun getStream(aggregateId: ID): EventStream<E, ID> =
    EventStreamInPostgresql(aggregateId, dataSource, objectToString, stringToObject)
}
