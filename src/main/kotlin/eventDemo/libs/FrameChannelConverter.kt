package eventDemo.libs

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Convert a [ReceiveChannel] of [Frame] to another [ReceiveChannel] of [object][T]
 */
@OptIn(ExperimentalCoroutinesApi::class)
inline fun <reified T> CoroutineScope.toObjectChannel(
  frames: ReceiveChannel<Frame>,
  bufferSize: Int = 0,
): ReceiveChannel<T> {
  val logger = KotlinLogging.logger { }
  return produce(capacity = bufferSize) {
    frames.consumeEach { frame ->
      if (frame is Frame.Text) {
        logger.debug { "Conversion of the Frame: ${frame.readText()}" }
        send(Json.decodeFromString(frame.readText()))
      } else {
        logger.warn { "The frame is not a text frame" }
      }
    }
  }
}

/**
 * Convert a [SendChannel] of [Frame] to another [SendChannel] of [object][T]
 */
inline fun <reified T> CoroutineScope.fromFrameChannel(frames: SendChannel<Frame>): SendChannel<T> {
  val channel = Channel<T>()
  launch {
    channel.consumeEach { obj ->
      frames.send(Frame.Text(Json.encodeToString(obj)))
    }
  }
  return channel
}
