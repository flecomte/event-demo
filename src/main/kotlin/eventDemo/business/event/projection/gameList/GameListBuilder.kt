package eventDemo.business.event.projection

import eventDemo.business.event.event.CardIsPlayedEvent
import eventDemo.business.event.event.GameEvent
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.event.PlayerChoseColorEvent
import eventDemo.business.event.event.PlayerHavePassEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.event.PlayerWinEvent

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
