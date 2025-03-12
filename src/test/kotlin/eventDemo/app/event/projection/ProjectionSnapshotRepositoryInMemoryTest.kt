package eventDemo.app.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.test.assertNotNull

@OptIn(DelicateCoroutinesApi::class)
class ProjectionSnapshotRepositoryInMemoryTest :
    FunSpec({

        test("when call applyAndPutToCache, the getUntil method must be use the built projection cache") {
            repeat(10) {
                val repo = getRepoTest()
                val aggregateId = IdTest()

                val eventOther = Event2Test(value2 = "valOther", version = 1, aggregateId = IdTest())
                repo.applyAndPutToCache(eventOther)
                assertNotNull(repo.getUntil(eventOther)).also {
                    assertNotNull(it.value) shouldBeEqual "valOther"
                }

                val event1 = Event1Test(value1 = "val1", version = 1, aggregateId = aggregateId)
                repo.applyAndPutToCache(event1)
                assertNotNull(repo.getLast(event1.aggregateId)).also {
                    assertNotNull(it.value) shouldBeEqual "val1"
                }
                assertNotNull(repo.getUntil(event1)).also {
                    assertNotNull(it.value) shouldBeEqual "val1"
                }

                val event2 = Event2Test(value2 = "val2", version = 2, aggregateId = aggregateId)
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

        test("ProjectionSnapshotRepositoryInMemory should be thread safe") {
            val repo = getRepoTest(2000)
            val aggregateId = IdTest()
            (1..10)
                .map { r ->
                    GlobalScope.launch {
                        repeat(10) {
                            val eventX = EventXTest(num = 1, version = r, aggregateId = aggregateId)
                            repo.applyAndPutToCache(eventX)
                        }
                    }
                }.joinAll()
            assertNotNull(repo.getLast(aggregateId)).num shouldBeEqual 100
        }
    })

@JvmInline
private value class IdTest(
    override val id: UUID = UUID.randomUUID(),
) : AggregateId

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

private fun getRepoTest(maxSnapshotCacheSize: Int = 2000): ProjectionSnapshotRepositoryInMemory<TestEvents, ProjectionTest, IdTest> =
    ProjectionSnapshotRepositoryInMemory(maxSnapshotCacheSize) { event ->
        (this ?: ProjectionTest(event.aggregateId)).let { projection ->
            when (event) {
                is Event1Test -> {
                    projection.copy(value = (projection.value ?: "") + event.value1)
                }

                is Event2Test -> {
                    projection.copy(value = (projection.value ?: "") + event.value2)
                }

                is EventXTest -> {
                    projection.copy(num = projection.num + event.num)
                }
            }
        }
    }
