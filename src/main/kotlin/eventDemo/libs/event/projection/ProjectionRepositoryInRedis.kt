package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.params.ScanParams
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

class ProjectionRepositoryInRedis<E : Event<ID>, P : Projection<ID>, ID : AggregateId>(
  private val jedis: UnifiedJedis,
  private val initialStateBuilder: (aggregateId: ID) -> P,
  private val projectionClass: KClass<P>,
  private val projectionToJson: (P) -> String,
  private val jsonToProjection: (String) -> P,
  applyToProjection: P.(event: E) -> P,
) : ProjectionRepositoryAbs<E, P, ID>(applyToProjection),
  ProjectionRepository<E, P, ID> {
  val logger = KotlinLogging.logger { }
  private val lock = ReentrantLock()

  /**
   * Get the list of all [Projections][Projection]
   */
  override fun getList(
    limit: Int,
    offset: Int,
  ): List<P> =
    jedis
      .hscan(
        projectionClass.redisHKey,
        offset.toString(),
        ScanParams()
          .match("*")
          .count(limit),
      ).result
      .mapNotNull {
        jsonToProjection(it.value)
      }

  /**
   * Get the [Projection].
   */
  override fun get(aggregateId: ID): P =
    jedis
      .hget(
        projectionClass.redisHKey,
        aggregateId.id.toString(),
      ).let {
        if (it == null || it == "nil") {
          initialStateBuilder(aggregateId)
        } else {
          jsonToProjection(it)
        }
      }

  override fun save(projection: P) {
    lock.withLock {
      if (get(projection.aggregateId).lastEventVersion < projection.lastEventVersion) {
        jedis.hset(
          projection.redisHKey,
          projection.aggregateId.id.toString(),
          projectionToJson(projection),
        )
        logger.info { "Projection saved" }
      } else {
        logger.error { "Projection save SKIP (an early version exists)" }
        error("Projection save SKIP (an early version exists)")
      }
    }
  }
}

private val <P : Projection<*>> KClass<P>.redisHKey: String get() =
  "projection:$simpleName"

private val <P : Projection<*>> P.redisHKey: String get() =
  this::class.redisHKey
