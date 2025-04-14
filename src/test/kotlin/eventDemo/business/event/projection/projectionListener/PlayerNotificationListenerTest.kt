package eventDemo.business.event.projection.projectionListener

import eventDemo.adapter.infrastructureLayer.event.projection.GameProjectionBusInMemory
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.projection.GameState
import eventDemo.business.notification.WelcomeToTheGameNotification
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
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
      val spyCall: () -> Unit = mockk(relaxed = true)
      PlayerNotificationListener(bus).startListening(player, gameId) {
        assertIs<WelcomeToTheGameNotification>(it)
        spyCall()
      }
      bus.publish(state)

      verify(exactly = 1) { spyCall() }
    }
  })
