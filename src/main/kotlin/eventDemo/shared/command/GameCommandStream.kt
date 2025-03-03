package eventDemo.shared.command

import eventDemo.app.actions.playNewCard.PlayCardCommand
import eventDemo.libs.command.CommandStreamInMemory

/**
 * A stream to publish and read the played card command.
 */
class GameCommandStream : CommandStreamInMemory<PlayCardCommand>()
