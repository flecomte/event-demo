package eventDemo.app.event

import eventDemo.app.GameState
import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.event.PlayerChoseColorEvent
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.app.event.event.PlayerReadyEvent

fun GameId.buildStateFromEventStream(eventStream: GameEventStream): GameState =
    buildStateFromEvents(
        eventStream.readAll(this),
    )

fun GameEvent.buildStateFromEventStreamTo(eventStream: GameEventStream): GameState =
    gameId.buildStateFromEvents(
        eventStream.readAll(gameId).takeWhile { it != this } + this,
    )

private fun GameId.buildStateFromEvents(events: List<GameEvent>): GameState =
    events.fold(GameState(this)) { state: GameState, event: GameEvent ->
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
        }
    }
