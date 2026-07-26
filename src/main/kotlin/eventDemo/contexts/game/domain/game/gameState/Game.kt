package eventDemo.contexts.game.domain.game.gameState

import eventDemo.contexts.game.domain.events.CardIsPlayedEvent
import eventDemo.contexts.game.domain.events.DrawFilledWithDiscardEvent
import eventDemo.contexts.game.domain.events.GameCreatedEvent
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.events.GameStartedEvent
import eventDemo.contexts.game.domain.events.NewPlayerEvent
import eventDemo.contexts.game.domain.events.PlayerHaveDrawCardEvent
import eventDemo.contexts.game.domain.events.PlayerReadyEvent
import eventDemo.contexts.game.domain.events.PlayerWinEvent
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.PlayerList
import eventDemo.contexts.game.domain.game.errors.GameException
import eventDemo.contexts.game.domain.game.errors.InconsistentEventVersionException

sealed interface Game {
  val aggregateId: GameId
  val players: PlayerList

  /**
   * On each modification, an event is put in their
   */
  val recordedEvents: Set<GameEvent>
  val version: Int

  enum class Direction {
    CLOCKWISE,
    COUNTER_CLOCKWISE,
    ;

    fun revert(): Direction =
      if (this === CLOCKWISE) {
        COUNTER_CLOCKWISE
      } else {
        CLOCKWISE
      }
  }

  companion object {
    fun loadFromHistory(events: Set<GameEvent>): Game =
      events
        .fold(GameInit(events.first().aggregateId)) { game: Game, event ->
          game.run {
            when (event) {
              is GameCreatedEvent if this is GameInit -> applyEvent(event)
              is GameCreatedEvent -> error("Game is already created")
              is NewPlayerEvent if this is GameCreated -> applyEvent(event)
              is NewPlayerEvent -> error("Game is already stared")
              is PlayerReadyEvent if this is GameCreated -> applyEvent(event)
              is PlayerReadyEvent -> error("Game is already stared")
              is GameStartedEvent if this is GameCreated -> applyEvent(event)
              is GameStartedEvent -> error("Game is already started")
              is CardIsPlayedEvent if this is GameStarted -> applyEvent(event)
              is CardIsPlayedEvent -> error("Game is end")
              is PlayerHaveDrawCardEvent if this is GameStarted -> applyEvent(event)
              is PlayerHaveDrawCardEvent -> error("Game is end")
              is PlayerWinEvent if this is GameStarted -> applyEvent(event)
              is PlayerWinEvent -> error("Game is end")
              is DrawFilledWithDiscardEvent if this is GameStarted -> applyEvent(event)
              is DrawFilledWithDiscardEvent -> error("Game is end")
            }
          }
        }.let {
          when (it) {
            is GameInit -> it
            is GameCreated -> it.copy(recordedEvents = emptySet())
            is GameEnded -> it.copy(recordedEvents = emptySet())
            is GameStarted -> it.copy(recordedEvents = emptySet())
          }
        }
  }
}

internal fun <T : GameEvent> T.checkState(
  block: (T) -> Boolean,
  exception: (T) -> GameException,
): T {
  if (!block(this)) throw exception(this)
  return this
}
