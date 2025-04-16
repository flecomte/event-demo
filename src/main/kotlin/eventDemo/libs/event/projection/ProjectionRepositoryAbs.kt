package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext

/**
 * Repository abstraction to declare common process
 */
abstract class ProjectionRepositoryAbs<E : Event<ID>, P : Projection<ID>, ID : AggregateId>(
  private val applyToProjection: P.(event: E) -> P,
) : ProjectionRepository<E, P, ID> {
  private val logger = KotlinLogging.logger {}

  /**
   * Update projection with the event.
   *
   * 1. get the last projection
   * 2. apply the new event to the projection
   */
  override fun apply(event: E): P =
    get(event.aggregateId).applyToProjectionSecure(event)

  /**
   * Update projection with the event, and save it.
   *
   * 1. get the last projection
   * 2. apply the new event to projection
   * 3. save it
   */
  override fun applyAndSave(event: E): P =
    apply(event)
      .also {
        withLoggingContext("projection" to it.toString(), "event" to event.toString()) {
          save(it)
        }
      }

  /**
   * Wrap the [applyToProjection] lambda to avoid duplicate apply of the same event.
   */
  protected val applyToProjectionSecure: P.(event: E) -> P = { event ->
    withLoggingContext("event" to event.toString(), "projection" to this.toString()) {
      if (canBeApply(event)) {
        applyToProjection(event)
      } else if (event.version <= lastEventVersion) {
        "Event is already in the Projection, skip apply.".let {
          logger.warn { it }
          error(it)
        }
      } else {
        "The version of the event must follow directly after the version of the projection.".let {
          logger.error { it }
          error(it)
        }
      }
    }
  }

  private fun P.canBeApply(event: E): Boolean =
    event.version == lastEventVersion + 1
}
