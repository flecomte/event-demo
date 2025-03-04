package eventDemo.shared

import eventDemo.app.actions.playNewCard.GameCommand
import eventDemo.shared.event.GameEvent
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json

fun Frame.Text.toEvent(): GameEvent = Json.decodeFromString(GameEvent.serializer(), readText())

fun GameEvent.toFrame(): Frame.Text = Frame.Text(Json.encodeToString(GameEvent.serializer(), this))

fun Frame.Text.toCommand(): GameCommand = Json.decodeFromString(GameCommand.serializer(), readText())

fun GameCommand.toFrame(): Frame.Text = Frame.Text(Json.encodeToString(GameCommand.serializer(), this))
