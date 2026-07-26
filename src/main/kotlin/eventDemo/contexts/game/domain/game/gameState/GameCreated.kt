package eventDemo.contexts.game.domain.game.gameState

import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.events.GameStartedEvent
import eventDemo.contexts.game.domain.events.NewPlayerEvent
import eventDemo.contexts.game.domain.events.PlayerReadyEvent
import eventDemo.contexts.game.domain.game.Card
import eventDemo.contexts.game.domain.game.DiscardPile
import eventDemo.contexts.game.domain.game.DrawPile
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.domain.game.PlayerHand
import eventDemo.contexts.game.domain.game.PlayerList
import eventDemo.contexts.game.domain.game.errors.AllPlayerNotReadyException
import eventDemo.contexts.game.domain.game.errors.DeckMissingCardsException
import eventDemo.contexts.game.domain.game.errors.NeedMorePlayersToStartGameException
import eventDemo.contexts.game.domain.game.errors.ThePlayerIsNotInTheGameException
import eventDemo.sharedKernel.UserId

data class GameCreated(
  override val aggregateId: GameId,
  override val players: PlayerList = PlayerList(),
  val playersStatus: Map<Player.PlayerId, PlayerStatus> = emptyMap(),
  override val recordedEvents: Set<GameEvent>,
  override val version: Int,
) : Game {
  val allPlayerIsReady: Boolean
    get() {
      return playersStatus.isNotEmpty() && playersStatus.values.all { it == PlayerStatus.Ready }
    }

  fun startGame(deck: Deck = newDeck().shuffleDeck()): GameStarted {
    val (drawPile, discardPile, playersHands) =
      initPiles(deck)
        .let { (drawPile, discardPile) ->
          createHandsFromDrawPile(drawPile)
            .let { (drawPile, playersHands) ->
              Triple(drawPile, discardPile, playersHands)
            }
        }

    return GameStartedEvent(
      aggregateId = aggregateId,
      firstPlayer = players.randomPlayer().id,
      version = version + 1,
      drawPile = drawPile,
      discardPile = discardPile,
      playersHans = playersHands,
    ).checkState(
      { players.size > 1 },
      { NeedMorePlayersToStartGameException(players) },
    ).checkState(
      { allPlayerIsReady },
      { AllPlayerNotReadyException(players) },
    ).checkState(
      { deck.size == 108 },
      { DeckMissingCardsException(players, deck) },
    ).also { if (it.drawPile.size + it.discardPile.size + playersHands.values.sumOf { it.size } != 108) error("missing cards!") }
      .run(::applyEvent)
  }

  private fun initPiles(deck: Set<Card>): Pair<DrawPile, DiscardPile> =
    DrawPile(deck)
      .generateValidDrawPile()
      .take(1)
      .let { (draw, cards) ->
        draw to DiscardPile(cards)
      }

  private fun createHandsFromDrawPile(drawPile: DrawPile) =
    players
      .map { it.id }
      .fold(Pair(drawPile, emptyMap<Player.PlayerId, PlayerHand>())) { (drawAcc, handsAcc), playerId ->
        drawAcc
          .take(7)
          .let { (draw, hand) ->
            Pair(
              draw,
              handsAcc + (playerId to PlayerHand(hand)),
            )
          }
      }

  fun userJoinTheGame(
    userId: UserId,
    name: String,
  ): GameCreated {
    if (players.map { it.userId }.contains(userId)) {
      throw IllegalStateException("User $userId already in party")
    }

    val player = Player(name, userId)

    return applyEvent(
      NewPlayerEvent(
        aggregateId = aggregateId,
        player = player,
        version = version + 1,
      ),
    )
  }

  fun setReadyPlayer(playerId: Player.PlayerId): GameCreated {
    if (!players.map { it.id }.contains(playerId)) {
      throw ThePlayerIsNotInTheGameException(playerId)
    }

    return PlayerReadyEvent(aggregateId, playerId, version + 1)
      .run(::applyEvent)
  }

  internal fun applyEvent(event: NewPlayerEvent): GameCreated =
    copy(
      players = players + (event.player),
      playersStatus = playersStatus + (event.player.id to PlayerStatus.Waiting),
      recordedEvents = recordedEvents + event,
      version = version + 1,
    )

  internal fun applyEvent(event: PlayerReadyEvent): GameCreated =
    copy(
      playersStatus = playersStatus + (event.playerId to PlayerStatus.Ready),
      recordedEvents = recordedEvents + event,
      version = version + 1,
    )

  internal fun applyEvent(event: GameStartedEvent): GameStarted =
    GameStarted(
      aggregateId = event.aggregateId,
      players =
        players
          .map {
            it.copy(hand = event.playersHans[it.id] ?: error("Player ${it.id} not found"))
          }.let { PlayerList(it.toSet()) },
      lastPlayerId = null,
      nextPlayerId = event.firstPlayer,
      drawPile = event.drawPile,
      discardPile = event.discardPile,
      version = event.version + 1,
      recordedEvents = recordedEvents + event,
      currentColor = event.discardPile.topCardColor ?: error("The discard pile was not initialized!"),
    )

  enum class PlayerStatus {
    Ready,
    Waiting,
  }
}

typealias Deck = Set<Card>

fun newDeck(): Deck =
  listOf(Card.Color.Red, Card.Color.Blue, Card.Color.Yellow, Card.Color.Green)
    .flatMap { color ->
      ((0..9) + (1..9)).map { Card.NumericCard(it, color) } +
        (1..2).map { Card.Plus2Card(color) } +
        (1..2).map { Card.ReverseCard(color) } +
        (1..2).map { Card.PassCard(color) }
    }.let {
      it + (1..4).map { Card.Plus4Card() }
    }.let {
      it + (1..4).map { Card.ChangeColorCard() }
    }.toSet()

fun Set<Card>.shuffleDeck(): Set<Card> {
  if (isDisabled) return this
  return shuffled().toSet()
}

private fun PlayerList.randomPlayer(): Player {
  if (isDisabled) return first()
  return random()
}

private var isDisabled = false

fun disableRandomForTest() {
  isDisabled = true
}
