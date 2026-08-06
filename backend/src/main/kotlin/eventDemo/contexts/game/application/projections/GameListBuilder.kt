package eventDemo.contexts.game.application.projections

import eventDemo.contexts.game.domain.events.CardIsPlayedEvent
import eventDemo.contexts.game.domain.events.DrawFilledWithDiscardEvent
import eventDemo.contexts.game.domain.events.GameCreatedEvent
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.events.GameStartedEvent
import eventDemo.contexts.game.domain.events.NewPlayerEvent
import eventDemo.contexts.game.domain.events.PlayerHaveDrawCardEvent
import eventDemo.contexts.game.domain.events.PlayerReadyEvent
import eventDemo.contexts.game.domain.events.PlayerWinEvent
import eventDemo.shared.game.projection.GameList

fun GameList.applyEvent(event: GameEvent): GameList =
  when (event) {
    is GameCreatedEvent -> {
      this
    }

    is NewPlayerEvent -> {
      copy(
        players = players + event.player,
        status = GameList.Status.OPENING,
      )
    }

    is GameStartedEvent -> {
      copy(
        status = GameList.Status.IS_STARTED,
      )
    }

    is PlayerWinEvent -> {
      copy(
        winners = winners,
        status = GameList.Status.FINISH,
      )
    }

    is CardIsPlayedEvent -> {
      this
    }

    is PlayerHaveDrawCardEvent -> {
      this
    }

    is PlayerReadyEvent -> {
      this
    }

    is DrawFilledWithDiscardEvent -> {
      this
    }
  }
