package eventDemo.testHelpers

import io.kotest.assertions.nondeterministic.eventually
import io.mockk.spyk
import io.mockk.verify
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

inline fun <R> arrange(block: () -> R): R =
  run(block)

inline fun <T, R> T.and(block: (T) -> R): R =
  this.let(block)

inline fun <T, R> T.act(block: (T) -> R): R =
  this.let(block)

inline fun <T, R> T.assert(block: (T) -> R): R =
  this.let(block)

suspend fun spyPing(
  duration: Duration = 0.5.seconds,
  exactly: Int = -1,
  block: (ping: () -> Unit) -> Unit,
) {
  val spy = spyk<() -> Unit>()

  block(spy)

  eventually(duration) {
    verify(exactly = exactly) { spy() }
  }
}
