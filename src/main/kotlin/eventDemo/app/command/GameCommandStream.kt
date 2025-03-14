package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.libs.command.CommandStream
import eventDemo.libs.command.CommandStreamChannel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * A stream to publish and read the game command.
 */
class GameCommandStream(
  incoming: ReceiveChannel<GameCommand>,
) : CommandStream<GameCommand> by CommandStreamChannel(incoming)
