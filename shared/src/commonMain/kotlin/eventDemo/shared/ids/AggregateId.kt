package eventDemo.shared.ids

import kotlin.uuid.Uuid

/**
 * Represent an ID for one aggregate, and it used in events
 * @see eventDemo.libs.eventSource.Event
 */
interface AggregateId {
  val id: Uuid
}
