package eventDemo.domain.event.projection

import eventDemo.domain.event.event.CardIsPlayedEvent
import eventDemo.domain.event.event.GameEvent
import eventDemo.domain.event.event.GameStartedEvent
import eventDemo.domain.event.event.NewPlayerEvent
import eventDemo.domain.event.event.PlayerChoseColorEvent
import eventDemo.domain.event.event.PlayerHavePassEvent
import eventDemo.domain.event.event.PlayerReadyEvent
import eventDemo.domain.event.event.PlayerWinEvent

fun GameList.apply(event: GameEvent): GameList =
  when (event) {
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
        winners = winners + event.player,
        status = GameList.Status.FINISH,
      )
    }

    is CardIsPlayedEvent -> {
      this
    }

    is PlayerChoseColorEvent -> {
      this
    }

    is PlayerHavePassEvent -> {
      this
    }

    is PlayerReadyEvent -> {
      this
    }
  }.copy(
    lastEventVersion = event.version,
  )
