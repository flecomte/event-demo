package eventDemo.app.event.projection

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.event.PlayerChoseColorEvent
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.event.PlayerWinEvent

fun GameId.buildStateFromEventStream(eventStream: GameEventStream): GameState {
    val events = eventStream.readAll(this)
    if (events.isEmpty()) return GameState(this)
    return events.buildStateFromEvents()
}

fun List<GameEvent>.buildStateFromEvents(): GameState {
    val gameId = this.firstOrNull()?.gameId ?: error("Cannot build GameState from an empty list")
    return fold(GameState(gameId)) { state, event ->
        state.apply(event)
    }
}

fun GameState.apply(event: GameEvent): GameState =
    let { state ->
        when (event) {
            is CardIsPlayedEvent -> {
                val direction =
                    when (event.card) {
                        is Card.ReverseCard -> state.direction.revert()
                        else -> state.direction
                    }

                val color =
                    when (event.card) {
                        is Card.ColorCard -> event.card.color
                        else -> state.lastColor
                    }

                state.copy(
                    lastPlayer = event.player,
                    direction = direction,
                    lastColor = color,
                    lastCard = GameState.LastCard(event.card, event.player),
                    deck = state.deck.putOneCardFromHand(event.player, event.card),
                )
            }

            is NewPlayerEvent -> {
                if (state.isStarted) error("The game is already started")

                state.copy(
                    players = state.players + event.player,
                )
            }

            is PlayerReadyEvent -> {
                state.copy(
                    readyPlayers = state.readyPlayers + event.player,
                )
            }

            is PlayerHavePassEvent -> {
                if (event.takenCard != state.deck.stack.first()) error("taken card is not ot top of the stack")
                state.copy(
                    lastPlayer = event.player,
                    deck = state.deck.takeOneCardFromStackTo(event.player),
                )
            }

            is PlayerChoseColorEvent -> {
                state.copy(
                    lastColor = event.color,
                )
            }

            is GameStartedEvent -> {
                state.copy(
                    lastColor = (event.deck.discard.first() as? Card.ColorCard)?.color ?: state.lastColor,
                    lastCard = GameState.LastCard(event.deck.discard.first(), event.firstPlayer),
                    lastPlayer = event.firstPlayer,
                    deck = event.deck,
                    isStarted = true,
                )
            }

            is PlayerWinEvent -> {
                copy(
                    playerWins = playerWins + event.player,
                )
            }
        }
    }
