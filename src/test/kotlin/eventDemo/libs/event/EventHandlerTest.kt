package eventDemo.libs.event

import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInMemory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual

class EventHandlerTest :
  FunSpec({
    test("EventHandler::handle should returns the built event") {
      val eventBus: Bus<EventXTest> = BusInMemory()
      val eventStore: EventStore<EventXTest, IdTest> = EventStoreInMemory()
      val versionBuilder: VersionBuilder = VersionBuilderLocal()
      val aggregateId: IdTest = IdTest()
      val handler =
        EventHandlerImpl(
          eventBus,
          eventStore,
          versionBuilder,
        )

      // When
      val event =
        handler.handle(aggregateId) {
          EventXTest(aggregateId = aggregateId, version = it, num = 1)
        }

      // Then
      event.aggregateId shouldBeEqual aggregateId
      event.version shouldBeEqual 1
    }

    xtest("EventHandler::handle should publish the event into the store")

    xtest("EventHandler::handle should publish the event into the bus")

    xtest("EventHandler::handle should publish the event into the bus in incremental order")
  })
