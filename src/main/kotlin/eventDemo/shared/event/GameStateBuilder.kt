package eventDemo.shared.event

import eventDemo.libs.event.EventStream
import eventDemo.shared.GameId
import eventDemo.shared.entity.Card

fun GameId.buildStateFromEventStream(eventStream: EventStream<GameEvent, GameId>): GameState =
    buildStateFromEvents(
        eventStream.readAll(this),
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
                )
            }

            is NewPlayerEvent -> {
                if (state.isReady) error("The game is already started")

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
                state.copy(
                    lastPlayer = event.player,
                )
            }

            is PlayerChoseColorEvent -> {
                state.copy(
                    lastColor = event.color,
                )
            }

            is GameStartedEvent -> {
                state.copy(
                    lastColor = (event.deck.discard.first() as? Card.ColorCard)?.color,
                    lastCard = GameState.LastCard(event.deck.discard.first(), event.firstPlayer),
                    lastPlayer = event.firstPlayer,
                    deck = event.deck,
                    isStarted = true,
                )
            }
        }
    }
