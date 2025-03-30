package eventDemo.business.event.projection

import eventDemo.cleanProjections
import eventDemo.configuration.serializer.UUIDSerializer
import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import eventDemo.libs.event.EventStore
import eventDemo.libs.event.EventStoreInMemory
import eventDemo.libs.event.VersionBuilderLocal
import eventDemo.libs.event.projection.DISABLED_CONFIG
import eventDemo.libs.event.projection.Projection
import eventDemo.libs.event.projection.ProjectionSnapshotRepository
import eventDemo.libs.event.projection.ProjectionSnapshotRepositoryInMemory
import eventDemo.libs.event.projection.ProjectionSnapshotRepositoryInRedis
import eventDemo.libs.event.projection.SnapshotConfig
import io.kotest.assertions.nondeterministic.continually
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.engine.names.WithDataTestName
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import redis.clients.jedis.JedisPooled
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class ProjectionSnapshotRepositoryTest :
  FunSpec({
    data class TestData(
      val store: EventStore<TestEvents, IdTest>,
      val snapshotRepo: ProjectionSnapshotRepository<TestEvents, ProjectionTest, IdTest>,
    ) : WithDataTestName {
      override fun dataTestName(): String =
        "${snapshotRepo::class.simpleName} with ${store::class.simpleName}"
    }

    val eventStores =
      listOf(
        { EventStoreInMemory<TestEvents, IdTest>() },
      )
    val projectionRepo =
      listOf(
        ::getSnapshotRepoInMemoryTest,
        ::getSnapshotRepoInRedisTest,
      )

    val list =
      eventStores.flatMap { store ->
        projectionRepo.map { repo ->
          store().let { store -> TestData(store, repo(store, DISABLED_CONFIG)) }
        }
      }

    context("when call applyAndPutToCache, the getUntil method must be use the built projection cache") {
      withData(list) { (eventStore, repo) ->
        val aggregateId = IdTest()

        val eventOther = Event2Test(value2 = "valOther", version = 1, aggregateId = IdTest())
        eventStore.publish(eventOther)
        repo.applyAndPutToCache(eventOther)
        assertNotNull(repo.getUntil(eventOther)).also {
          assertNotNull(it.value) shouldBeEqual "valOther"
        }

        val event1 = Event1Test(value1 = "val1", version = 1, aggregateId = aggregateId)
        eventStore.publish(event1)
        repo.applyAndPutToCache(event1)
        assertNotNull(repo.getLast(event1.aggregateId)).also {
          assertNotNull(it.value) shouldBeEqual "val1"
        }
        assertNotNull(repo.getUntil(event1)).also {
          assertNotNull(it.value) shouldBeEqual "val1"
        }

        val event2 = Event2Test(value2 = "val2", version = 2, aggregateId = aggregateId)
        eventStore.publish(event2)
        repo.applyAndPutToCache(event2)
        assertNotNull(repo.getLast(event2.aggregateId)).also {
          assertNotNull(it.value) shouldBeEqual "val1val2"
        }
        assertNotNull(repo.getUntil(event1)).also {
          assertNotNull(it.value) shouldBeEqual "val1"
        }
        assertNotNull(repo.getUntil(event2)).also {
          assertNotNull(it.value) shouldBeEqual "val1val2"
        }
      }
    }

    context("getList method must be return all inserted events") {
      withData(list) { (eventStore, repo) ->
        val aggregateId = IdTest()
        val otherAggregateId = IdTest()

        val eventOther = Event2Test(value2 = "valOther", version = 1, aggregateId = otherAggregateId)
        eventStore.publish(eventOther)
        repo.applyAndPutToCache(eventOther)
        assertNotNull(repo.getUntil(eventOther)).also {
          assertNotNull(it.value) shouldBeEqual "valOther"
        }

        val event1 = Event1Test(value1 = "val1", version = 1, aggregateId = aggregateId)
        eventStore.publish(event1)
        repo.applyAndPutToCache(event1)

        val event2 = Event2Test(value2 = "val2", version = 2, aggregateId = aggregateId)
        eventStore.publish(event2)
        repo.applyAndPutToCache(event2)

        repo.getList().apply {
          any { it.aggregateId == otherAggregateId } shouldBeEqual true
          any { it.aggregateId == aggregateId } shouldBeEqual true
          any { it.value == "val1val2" } shouldBeEqual true
          any { it.value == "valOther" } shouldBeEqual true
          any { it.lastEventVersion == 2 } shouldBeEqual true
          any { it.lastEventVersion == 1 } shouldBeEqual true
        }
      }
    }

    context("ProjectionSnapshotRepository should be thread safe") {
      continually(1.seconds) {
        withData(list) { (eventStore, repo) ->
          val aggregateId = IdTest()
          val versionBuilder = VersionBuilderLocal()
          val lock = ReentrantLock()
          (0..9)
            .map {
              GlobalScope.launch {
                (1..10).forEach {
                  val eventX =
                    lock.withLock {
                      EventXTest(num = 1, version = versionBuilder.buildNextVersion(aggregateId), aggregateId = aggregateId)
                        .also { eventStore.publish(it) }
                    }
                  repo.applyAndPutToCache(eventX)
                }
              }
            }.joinAll()
          assertNotNull(repo.getLast(aggregateId)).num shouldBeEqual 100
          assertNotNull(repo.count(aggregateId)) shouldBeEqual 100
        }
      }
    }

    context("removeOldSnapshot") {
      withData(list) { (eventStore, repo) ->
        val versionBuilder = VersionBuilderLocal()
        val aggregateId = IdTest()

        fun buildEndSendEventX() {
          EventXTest(num = 1, version = versionBuilder.buildNextVersion(aggregateId), aggregateId = aggregateId)
            .also { eventStore.publish(it) }
            .also { repo.applyAndPutToCache(it) }
        }

        buildEndSendEventX()
        repo.getLast(aggregateId).num shouldBeEqual 1
        buildEndSendEventX()
        repo.getLast(aggregateId).num shouldBeEqual 2
        buildEndSendEventX()
        repo.getLast(aggregateId).num shouldBeEqual 3
        buildEndSendEventX()
        repo.getLast(aggregateId).num shouldBeEqual 4
      }
    }
  })

@JvmInline
@Serializable
private value class IdTest(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
) : AggregateId

@Serializable
private data class ProjectionTest(
  override val aggregateId: IdTest,
  override val lastEventVersion: Int = 0,
  var value: String? = null,
  var num: Int = 0,
) : Projection<IdTest>

private sealed interface TestEvents : Event<IdTest>

private data class Event1Test(
  override val eventId: UUID = UUID.randomUUID(),
  override val aggregateId: IdTest,
  override val createdAt: Instant = Clock.System.now(),
  override val version: Int,
  val value1: String,
) : TestEvents

private data class Event2Test(
  override val eventId: UUID = UUID.randomUUID(),
  override val aggregateId: IdTest,
  override val createdAt: Instant = Clock.System.now(),
  override val version: Int,
  val value2: String,
) : TestEvents

private data class EventXTest(
  override val eventId: UUID = UUID.randomUUID(),
  override val aggregateId: IdTest,
  override val createdAt: Instant = Clock.System.now(),
  override val version: Int,
  val num: Int,
) : TestEvents

private fun getSnapshotRepoInMemoryTest(
  eventStore: EventStore<TestEvents, IdTest>,
  snapshotConfig: SnapshotConfig,
): ProjectionSnapshotRepository<TestEvents, ProjectionTest, IdTest> =
  ProjectionSnapshotRepositoryInMemory(
    eventStore = eventStore,
    initialStateBuilder = { aggregateId: IdTest -> ProjectionTest(aggregateId) },
    snapshotCacheConfig = snapshotConfig,
    applyToProjection = apply,
  )

private fun getSnapshotRepoInRedisTest(
  eventStore: EventStore<TestEvents, IdTest>,
  snapshotConfig: SnapshotConfig,
): ProjectionSnapshotRepository<TestEvents, ProjectionTest, IdTest> {
  val jedis = JedisPooled("redis://localhost:6379")
  jedis.cleanProjections()
  return ProjectionSnapshotRepositoryInRedis(
    eventStore = eventStore,
    jedis = jedis,
    initialStateBuilder = { aggregateId: IdTest -> ProjectionTest(aggregateId) },
    snapshotCacheConfig = snapshotConfig,
    projectionClass = ProjectionTest::class,
    projectionToJson = { Json.encodeToString(it) },
    jsonToProjection = { Json.decodeFromString(it) },
    applyToProjection = apply,
  )
}

private val apply: ProjectionTest.(TestEvents) -> ProjectionTest = { event ->
  this.let { projection ->
    when (event) {
      is Event1Test -> {
        projection.copy(value = (projection.value.orEmpty()) + event.value1)
      }

      is Event2Test -> {
        projection.copy(value = (projection.value.orEmpty()) + event.value2)
      }

      is EventXTest -> {
        projection.copy(num = projection.num + event.num)
      }
    }.copy(
      lastEventVersion = event.version,
    )
  }
}
