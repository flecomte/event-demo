package eventDemo.testHelpers

import eventDemo.contexts.auth.domain.User
import eventDemo.contexts.game.application.command.handlers.GameCommandHandlerDispatcher
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.shared.game.Card
import eventDemo.shared.game.Player
import eventDemo.shared.game.command.JoinTheGameCommand
import eventDemo.shared.game.command.PlayCardCommand
import eventDemo.shared.game.command.ReadyToPlayCommand
import eventDemo.shared.ids.GameId
import org.koin.core.Koin
import java.util.UUID
import kotlin.uuid.toKotlinUuid

object CreateGameWithCommandsHelpers {
  class Data(
    private val repo: GameRepository,
    val gameId: GameId,
  ) {
    fun getPlayer(user: User): Player =
      repo.get(gameId)!!.players.get(user.id)
  }

  context(koin: Koin)
  fun <T> createGameWithCommands(
    gameName: String = "testGame${UUID.randomUUID()}",
    block: context(CreateGameWithCommandsHelpers, GameCommandHandlerDispatcher) Data.() -> T,
  ): T {
    val gameId = GameId(UUID.nameUUIDFromBytes(gameName.encodeToByteArray()).toKotlinUuid())
    val repo = koin.get<GameRepository>()
    repo.create(gameId)

    return koin.get<GameCommandHandlerDispatcher>().run {
      with(CreateGameWithCommandsHelpers) {
        Data(repo, gameId).block()
      }
    }
  }

  context(dispatcher: GameCommandHandlerDispatcher, data: Data)
  fun User.joinTheGame(): JoinTheGameCommand =
    JoinTheGameCommand(
      id,
      JoinTheGameCommand.Payload(data.gameId),
    ).also { dispatcher.dispatch(it) }

  context(dispatcher: GameCommandHandlerDispatcher, data: Data)
  fun Player.readyToPlay(): ReadyToPlayCommand =
    ReadyToPlayCommand(
      userId,
      ReadyToPlayCommand.Payload(data.gameId, id),
    ).also { dispatcher.dispatch(it) }

  context(dispatcher: GameCommandHandlerDispatcher, data: Data)
  fun Player.playCard(
    card: Card,
    chosenColor: Card.Color? = null,
  ): PlayCardCommand =
    PlayCardCommand(
      userId,
      PlayCardCommand.Payload(data.gameId, id, card, chosenColor),
    ).also { dispatcher.dispatch(it) }
}
