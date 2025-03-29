package eventDemo.libs.event

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

@DelicateCoroutinesApi
class EventStreamInMemoryTest :
  FunSpec({
    fun streamWith3Events(block: EventStream<Event<IdTest>>.(id: IdTest) -> Unit): EventStream<Event<IdTest>> =
      EventStreamInMemory(IdTest()).apply {
        publish(EventXTest(aggregateId = aggregateId, version = 1, num = 1))
        publish(EventXTest(aggregateId = aggregateId, version = 2, num = 2))
        publish(EventXTest(aggregateId = aggregateId, version = 3, num = 3))
        block(aggregateId)
      }

    test("readVersionBetween should only return the event of aggregate") {
      streamWith3Events {
        readVersionBetween(1..2) shouldHaveSize 2
        readVersionBetween(1..1) shouldHaveSize 1
        readVersionBetween(2..20) shouldHaveSize 2
        readVersionBetween(4..20) shouldHaveSize 0
      }
    }

    test("readAll should only return the event of aggregate") {
      streamWith3Events {
        readAll() shouldHaveSize 3
        readAll().also {
          it.forEachIndexed { i, event ->
            event.version shouldBeEqual i + 1
          }
        }
      }
    }

    test("getByVersion should only return the event with this version") {
      streamWith3Events {
        assertNotNull(getByVersion(1)).version shouldBeEqual 1
        assertNotNull(getByVersion(2)).version shouldBeEqual 2
        assertNotNull(getByVersion(3)).version shouldBeEqual 3
        assertNull(getByVersion(4))
      }
    }

    test("readGreaterOfVersion should only return the events with greater version") {
      streamWith3Events {
        assertNotNull(readGreaterOfVersion(1)) shouldHaveSize 2
        assertNotNull(readGreaterOfVersion(2)) shouldHaveSize 1
        assertNotNull(readGreaterOfVersion(3)) shouldHaveSize 0
        assertNotNull(readGreaterOfVersion(30)) shouldHaveSize 0
      }
    }

    test("publish should be throw error when publish another aggregate event") {
      EventStreamInMemory(IdTest()).apply {
        assertThrows<EventStreamPublishException> { publish(EventXTest(aggregateId = IdTest(), version = 1, num = 1)) }
      }
    }

    test("publish should be concurrently secure") {
      val id = IdTest()
      val stream = EventStreamInMemory(id)
      (1..10).forEach { i1 ->
        GlobalScope.launch {
          (1..10).forEach { i2 ->
            stream.publish(
              EventXTest(
                aggregateId = id,
                version = (i1 * 10) + i2,
                num = (i1 * 10) + i2,
              ),
            )
          }
        }
      }
    }
  })
