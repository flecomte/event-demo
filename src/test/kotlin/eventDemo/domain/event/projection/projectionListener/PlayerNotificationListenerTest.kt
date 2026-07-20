package eventDemo.domain.event.projection.projectionListener

import eventDemo.adapter.infrastructure.event.projection.GameProjectionBusInMemory
import eventDemo.domain.entity.GameId
import eventDemo.domain.entity.Player
import eventDemo.domain.event.event.NewPlayerEvent
import eventDemo.domain.event.projection.GameState
import eventDemo.domain.notification.WelcomeToTheGameNotification
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.assertIs

class PlayerNotificationListenerTest :
  FunSpec({

    test("startListening should react when a projection is sent to the bus") {
      val player = Player("Tesla")
      val gameId = GameId()
      val bus = GameProjectionBusInMemory()
      val state =
        GameState(
          aggregateId = gameId,
          lastEvent = NewPlayerEvent(gameId, player, 1),
          players = setOf(player),
        )
      val spy = spyk<() -> Unit>()
      PlayerNotificationListener(bus).startListening(player, gameId) {
        assertIs<WelcomeToTheGameNotification>(it)
        spy()
      }
      bus.publish(state)

      verify(exactly = 1) { spy() }
    }
  })
