package eventDemo.business.event

import eventDemo.adapter.infrastructureLayer.event.GameEventBusInMemory
import eventDemo.adapter.infrastructureLayer.event.GameEventStoreInMemory
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.event.GameEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.libs.event.VersionBuilderLocal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GameEventHandlerTest :
  FunSpec({
    test("handle event should publish the event to the stream") {
      // Given
      val eventBus: GameEventBus = GameEventBusInMemory()
      val eventStore: GameEventStore = spyk(GameEventStoreInMemory())
      val handler =
        GameEventHandler(
          eventBus = eventBus,
          eventStore = eventStore,
          versionBuilder = VersionBuilderLocal(),
        )
      val gameId = GameId()
      val player1 = Player("Tesla")

      // When
      handler.handle(gameId) { NewPlayerEvent(gameId, player1, it) }

      // Then
      coVerify(exactly = 1) { eventStore.publish(any()) }

      eventStore.getStream(gameId).readAll().let { events ->
        events shouldHaveSize 1
        events.first().let {
          assertIs<NewPlayerEvent>(it)
          it.aggregateId shouldBeEqual gameId
          it.player.name shouldBeEqual "Tesla"
        }
      }
    }

    test("handle event should publish the event to the bus") {
      // Given
      val eventBus: GameEventBus = spyk(GameEventBusInMemory())
      val eventStore: GameEventStore = GameEventStoreInMemory()
      val handler =
        GameEventHandler(
          eventBus = eventBus,
          eventStore = eventStore,
          versionBuilder = VersionBuilderLocal(),
        )
      val gameId = GameId()
      val player1 = Player("Tesla")

      // When
      var event: GameEvent? = null
      val spy = spyk(mockk<() -> Unit>())
      eventBus.subscribe {
        spy()
        event = it
      }
      handler.handle(gameId) { NewPlayerEvent(gameId, player1, it) }

      // Then
      verify(exactly = 1) { spy() }
      coVerify(exactly = 1) { eventBus.publish(any()) }

      assertNotNull(event).let {
        assertIs<NewPlayerEvent>(it)
        it.aggregateId shouldBeEqual gameId
        it.player.name shouldBeEqual "Tesla"
      }
    }

    test("handle event should call version builder once") {
      // Given
      val eventBus: GameEventBus = GameEventBusInMemory()
      val eventStore: GameEventStore = GameEventStoreInMemory()
      val versionBuilder = spyk(VersionBuilderLocal())
      val handler =
        GameEventHandler(
          eventBus = eventBus,
          eventStore = eventStore,
          versionBuilder = versionBuilder,
        )
      val gameId = GameId()
      val player1 = Player("Tesla")

      // When
      handler.handle(gameId) { NewPlayerEvent(gameId, player1, it) }

      // Then
      verify(exactly = 1) { versionBuilder.buildNextVersion(any()) }

      eventStore
        .getStream(gameId)
        .readAll()
        .first()
        .version shouldBeEqual 1
    }
  })
