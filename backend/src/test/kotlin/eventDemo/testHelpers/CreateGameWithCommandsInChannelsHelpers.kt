package eventDemo.testHelpers

import eventDemo.contexts.auth.domain.User
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.domain.game.gameState.Game
import eventDemo.shared.game.Card
import eventDemo.shared.game.Player
import eventDemo.shared.game.command.GameCommand
import eventDemo.shared.game.command.JoinTheGameCommand
import eventDemo.shared.game.command.PlayCardCommand
import eventDemo.shared.game.command.ReadyToPlayCommand
import eventDemo.shared.ids.GameId
import kotlinx.coroutines.channels.Channel
import org.koin.core.Koin

object CreateGameWithCommandsInChannelsHelpers {
  class Data(
    private val repo: GameRepository,
    val gameId: GameId,
    val currentUser: User,
  ) {
    fun getPlayer(user: User): Player =
      game.players.get(user.id)

    val currentPlayer: Player get() = getPlayer(currentUser)

    val game: Game
      get() = repo.get(gameId)!!
  }

  context(koin: Koin)
  suspend fun <T> createGameWithCommandsInChannels(
    channelCommand: Channel<GameCommand>,
    gameId: GameId,
    user: User,
    block:
      suspend context(
        CreateGameWithCommandsInChannelsHelpers,
        Channel<GameCommand>,
        User,
      ) Data.() -> T,
  ): T {
    val repo = koin.get<GameRepository>()
    repo.getOrCreate(gameId)

    return with(channelCommand) {
      with(user) {
        with(CreateGameWithCommandsInChannelsHelpers) {
          Data(repo, gameId, user).block()
        }
      }
    }
  }

  context(channelCommand: Channel<GameCommand>, data: Data)
  suspend fun joinTheGame(): JoinTheGameCommand =
    JoinTheGameCommand(
      data.currentUser.id,
      JoinTheGameCommand.Payload(data.gameId),
    ).also { channelCommand.send(it) }

  context(channelCommand: Channel<GameCommand>, data: Data)
  suspend fun readyToPlay(): ReadyToPlayCommand =
    ReadyToPlayCommand(
      data.currentUser.id,
      ReadyToPlayCommand.Payload(data.gameId, data.currentPlayer.id),
    ).also { channelCommand.send(it) }

  context(channelCommand: Channel<GameCommand>, data: Data)
  suspend fun playCard(
    card: Card,
    chosenColor: Card.Color? = null,
  ): PlayCardCommand =
    PlayCardCommand(
      data.currentUser.id,
      PlayCardCommand.Payload(data.gameId, data.currentPlayer.id, card, chosenColor),
    ).also { channelCommand.send(it) }
}
