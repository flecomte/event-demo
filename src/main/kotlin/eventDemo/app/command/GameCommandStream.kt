package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.libs.command.CommandStream
import eventDemo.libs.command.CommandStreamChannel
import eventDemo.libs.command.CommandStreamInMemory
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json

/**
 * A stream to publish and read the game command.
 */
class GameCommandStreamInMemory : CommandStreamInMemory<GameCommand>()

/**
 * A stream to publish and read the game command.
 */
class GameCommandStream(
    incoming: ReceiveChannel<Frame>,
    outgoing: SendChannel<Frame>,
) : CommandStream<GameCommand> by CommandStreamChannel(
        incoming,
        outgoing,
        { Json.encodeToString(GameCommand.serializer(), it) },
        { Json.decodeFromString(GameCommand.serializer(), it) },
    )
