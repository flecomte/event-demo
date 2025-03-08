package eventDemo.shared

import eventDemo.app.command.command.GameCommand
import eventDemo.app.event.event.GameEvent
import eventDemo.app.notification.Notification
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json

fun Frame.Text.toEvent(): GameEvent = Json.decodeFromString(GameEvent.serializer(), readText())

fun GameEvent.toFrame(): Frame.Text = Frame.Text(Json.encodeToString(GameEvent.serializer(), this))

fun Frame.Text.toCommand(): GameCommand = Json.decodeFromString(GameCommand.serializer(), readText())

fun GameCommand.toFrame(): Frame.Text = Frame.Text(Json.encodeToString(GameCommand.serializer(), this))

fun Frame.toNotification(): Notification =
    Json.decodeFromString(
        Notification.serializer(),
        (this as Frame.Text).readText(),
    )

fun Notification.toFrame(): Frame.Text = Frame.Text(Json.encodeToString(Notification.serializer(), this))
