package eventDemo.libs.event

import eventDemo.Tag
import eventDemo.testKoinApplicationWithConfig
import io.kotest.common.KotestInternal
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.koin.core.Koin
import kotlin.test.assertNotNull

@OptIn(KotestInternal::class, DelicateCoroutinesApi::class)
class EventStreamTest :
  FunSpec({
    tags(Tag.Postgresql)

    fun EventStream<EventXTest, IdTest>.with3Events(block: EventStream<EventXTest, IdTest>.(id: IdTest) -> Unit) =
      also {
        publish(EventXTest(aggregateId = aggregateId, version = 1, num = 1))
        publish(EventXTest(aggregateId = aggregateId, version = 2, num = 2))
        publish(EventXTest(aggregateId = aggregateId, version = 3, num = 3))
        block(aggregateId)
      }

    fun Koin.eventStreams(): List<EventStream<EventXTest, IdTest>> =
      listOf(
        EventStreamInMemory(IdTest()),
        EventStreamInPostgresql(
          IdTest(),
          dataSource = get(),
          objectToString = { Json.encodeToString(it) },
          stringToObject = { Json.decodeFromString(it) },
        ),
      )

    context("readVersionBetween should only return the event of aggregate") {
      testKoinApplicationWithConfig {
        withData(eventStreams()) { stream ->
          stream.with3Events {
            readVersionBetween(1..2) shouldHaveSize 2
            readVersionBetween(1..1) shouldHaveSize 1
            readVersionBetween(2..20) shouldHaveSize 2
            readVersionBetween(4..20) shouldHaveSize 0
          }
        }
      }
    }

    context("readAll should only return the event of aggregate") {
      testKoinApplicationWithConfig {
        withData(eventStreams()) { stream ->
          stream.with3Events {
            readAll() shouldHaveSize 3
            readAll().also {
              it.forEachIndexed { i, event ->
                event.version shouldBeEqual i + 1
              }
            }
          }
        }
      }
    }

    context("getByVersion should only return the event with this version") {
      testKoinApplicationWithConfig {
        withData(eventStreams()) { stream ->
          stream.with3Events {
            assertNotNull(getByVersion(1)).version shouldBeEqual 1
            assertNotNull(getByVersion(2)).version shouldBeEqual 2
            assertNotNull(getByVersion(3)).version shouldBeEqual 3
            assertNull(getByVersion(4))
          }
        }
      }
    }

    context("readGreaterOfVersion should only return the events with greater version") {
      testKoinApplicationWithConfig {
        withData(eventStreams()) {
          it.with3Events {
            assertNotNull(readGreaterOfVersion(1)) shouldHaveSize 2
            assertNotNull(readGreaterOfVersion(2)) shouldHaveSize 1
            assertNotNull(readGreaterOfVersion(3)) shouldHaveSize 0
            assertNotNull(readGreaterOfVersion(30)) shouldHaveSize 0
          }
        }
      }
    }

    context("publish should be throw error when publish another aggregate event") {
      testKoinApplicationWithConfig {
        withData(eventStreams()) {
          assertThrows<EventStreamPublishException> { it.publish(EventXTest(aggregateId = IdTest(), version = 1, num = 1)) }
        }
      }
    }

    context("publish should be concurrently secure").config(tags = setOf(Tag.Concurrence)) {
      testKoinApplicationWithConfig {
        withData(eventStreams()) { stream ->
          (0..9)
            .map { i1 ->
              GlobalScope.launch {
                (1..10).forEach { i2 ->
                  stream.publish(
                    EventXTest(
                      aggregateId = stream.aggregateId,
                      version = (i1 * 10) + i2,
                      num = (i1 * 10) + i2,
                    ),
                  )
                }
              }
            }.joinAll()
          stream.readAll() shouldHaveSize 100
        }
      }
    }
  })
