package eventDemo.libs.eventSource.eventStore

import eventDemo.libs.eventSource.AggregateId
import eventDemo.libs.eventSource.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.postgresql.util.PGobject
import org.postgresql.util.PSQLException
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
  private val tableName: String,
) : EventStream<E, ID> {
  private val logger = KotlinLogging.logger {}

  override fun append(event: E) {
    withLoggingContext("event" to event.toString()) {
      if (event.aggregateId != aggregateId) {
        throw EventStreamPublishException(
          "You cannot publish this event in this stream because it has a different aggregateId!",
        )
      }
      try {
        dataSource.connection.use { connection ->
          connection
            .prepareStatement(
              """
              insert into $tableName (id, aggregate_id, version, data)
              values (?, ?, ?, ?)
              on conflict (id) do nothing
              """.trimIndent(),
            ).use {
              it.setObject(1, event.eventId.id)
              it.setObject(2, event.aggregateId.id)
              it.setInt(3, event.version)
              it.setObject(4, PGJsonb(objectToString(event)))
              it.executeUpdate()
            }
        }
      } catch (e: PSQLException) {
        if (e.serverErrorMessage?.constraint == "game_event_stream_aggregate_id_version_key") {
          logger.warn { "duplicate version" }
          throw VersionConflictException(event)
        } else {
          throw e
        }
      }
      logger.info { "Event appended" }
    }
  }

  override fun readAll(): Set<E> =
    dataSource.connection.use { connection ->
      connection
        .prepareStatement(
          """
          select data
          from $tableName
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

  override fun exist(): Boolean =
    dataSource.connection.use { connection ->
      connection
        .prepareStatement(
          """
          select 1
          from $tableName
          where aggregate_id = ?
          limit 1
          order by version asc
          """.trimIndent(),
        ).use {
          it.setObject(1, aggregateId.id)
          it.executeQuery().use { resultSet ->
            resultSet.next()
          }
        }
    }

  override fun readVersionBetween(version: IntRange): Set<E> =
    dataSource.connection.use { connection ->
      connection
        .prepareStatement(
          """
          select data
          from $tableName
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
