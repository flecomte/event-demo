package eventDemo.shared.command

import eventDemo.app.actions.playNewCard.PlayCardCommand
import eventDemo.libs.command.CommandStreamInMemory

class GameCommandStream : CommandStreamInMemory<PlayCardCommand>()
