package eventDemo.libs.event

import io.github.oshai.kotlinlogging.KotlinLogging
import org.postgresql.util.PGobject
import javax.sql.DataSource

/**
 * An In-Memory implementation of an event stream.
 *
 * All methods are implemented.
 */
class EventStreamInPostgresql<E : Event<ID>, ID : AggregateId>(
  override val aggregateId: ID,
  private val dataSource: DataSource,
  private val objectToString: (E) -> String,
  private val stringToObject: (String) -> E,
) : EventStream<E, ID> {
  private val logger = KotlinLogging.logger {}

  override fun publish(event: E) {
    if (event.aggregateId != aggregateId) {
      throw EventStreamPublishException(
        "You cannot publish this event in this stream because it has a different aggregateId!",
      )
    }
    dataSource.connection.use { connection ->
      connection
        .prepareStatement(
          """
          insert into event_stream(id, aggregate_id, version, data) 
          values (?, ?, ?, ?)
          """.trimIndent(),
        ).use {
          it.setObject(1, event.eventId)
          it.setObject(2, event.aggregateId.id)
          it.setInt(3, event.version)
          it.setObject(4, PGJsonb(objectToString(event)))
          it.executeUpdate()
        }
    }
    logger.info { "Event published" }
  }

  override fun readAll(): Set<E> =
    dataSource.connection.use { connection ->
      connection
        .prepareStatement(
          """
          select data 
          from event_stream
          where aggregate_id = ?
          order by version asc
          """.trimIndent(),
        ).use {
          it.setObject(1, aggregateId.id)
          it.executeQuery().use { resultSet ->
            buildSet {
              while (resultSet.next()) {
                resultSet
                  .getString("data")
                  .let(stringToObject)
                  .let { add(it) }
              }
            }
          }
        }
    }

  override fun readVersionBetween(version: IntRange): Set<E> =
    dataSource.connection.use { connection ->
      connection
        .prepareStatement(
          """
          select data
          from event_stream
          where version between ? and ? 
            and aggregate_id = ?
          order by version asc
          """.trimIndent(),
        ).use { stmt ->
          stmt.setInt(1, version.first)
          stmt.setInt(2, version.last)
          stmt.setObject(3, aggregateId.id)
          stmt.executeQuery().use { resultSet ->
            buildSet {
              while (resultSet.next()) {
                resultSet
                  .getString("data")
                  .let(stringToObject)
                  .let { add(it) }
              }
            }
          }
        }
    }
}

class PGJsonb(
  value: String,
) : PGobject() {
  init {
    this.value = value
    this.type = "jsonb"
  }
}
