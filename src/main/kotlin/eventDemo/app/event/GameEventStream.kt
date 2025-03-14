package eventDemo.app.event

import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.EventStream

/**
 * A stream to publish and read the played card event.
 */
class GameEventStream(
  private val eventStream: EventStream<GameEvent>,
) : EventStream<GameEvent> by eventStream {
  override fun publish(event: GameEvent) {
    eventStream.publish(event)
  }
}
