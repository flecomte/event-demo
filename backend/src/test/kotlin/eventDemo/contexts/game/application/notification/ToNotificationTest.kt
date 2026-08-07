package eventDemo.contexts.game.application.notification

import eventDemo.contexts.game.domain.events.CardIsPlayedEvent
import eventDemo.contexts.game.domain.events.DrawFilledWithDiscardEvent
import eventDemo.contexts.game.domain.events.GameStartedEvent
import eventDemo.contexts.game.domain.events.NewPlayerEvent
import eventDemo.contexts.game.domain.events.PlayerHaveDrawCardEvent
import eventDemo.contexts.game.domain.events.PlayerReadyEvent
import eventDemo.contexts.game.domain.game.gameState.GameCreated
import eventDemo.contexts.game.domain.game.gameState.GameStarted
import eventDemo.shared.game.Card
import eventDemo.shared.game.DiscardPile
import eventDemo.shared.game.DrawPile
import eventDemo.shared.game.Player
import eventDemo.shared.game.PlayerHand
import eventDemo.shared.game.PlayerList
import eventDemo.shared.game.notification.ItsTheTurnOfNotification
import eventDemo.shared.game.notification.PilesShuffledNotification
import eventDemo.shared.game.notification.PlayerAsPlayACardNotification
import eventDemo.shared.game.notification.PlayerHavePassNotification
import eventDemo.shared.game.notification.PlayerWasReadyNotification
import eventDemo.shared.game.notification.TheGameWasStartedNotification
import eventDemo.shared.game.notification.WelcomeToTheGameNotification
import eventDemo.shared.game.notification.YourNewCardNotification
import eventDemo.shared.ids.GameId
import eventDemo.shared.ids.UserId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertInstanceOf

class ToNotificationTest :
  FunSpec({
    val player1 =
      Player(
        name = "Bob",
        userId = UserId(),
        hand = PlayerHand(setOf(Card.NumericCard(1, Card.Color.Red))),
        id = Player.PlayerId(),
      )
    val player2 =
      Player(
        name = "John",
        userId = UserId(),
        hand = PlayerHand(setOf(Card.NumericCard(1, Card.Color.Red))),
        id = Player.PlayerId(),
      )

    test("NewPlayerEvent") {
      val game =
        GameCreated(
          aggregateId = GameId(),
          version = 1,
          players = PlayerList(setOf(player1)),
          recordedEvents = setOf(),
        )
      NewPlayerEvent(
        game.aggregateId,
        version = 2,
        player = player1,
      ).toNotification(
        game = game,
        currentUserId = player1.userId,
      ).let {
        it.toList().size shouldBe 1
        // Check if the user is
        assertInstanceOf<WelcomeToTheGameNotification>(it.first()).run {
          players.size shouldBe 1
          players.first().name shouldBe "Bob"
        }
      }
    }

    test("PlayerReadyEvent") {
      val game =
        GameCreated(
          aggregateId = GameId(),
          version = 1,
          players = PlayerList(setOf(player1)),
          recordedEvents = setOf(),
        )
      PlayerReadyEvent(
        game.aggregateId,
        version = 2,
        playerId = player1.id,
      ).toNotification(
        game = game,
        currentUserId = player1.userId,
      ).let {
        it.toList().size shouldBe 1
        assertInstanceOf<PlayerWasReadyNotification>(it.first()).let {
          it.playerId shouldBe player1.id
        }
      }
    }

    test("PlayerHaveDrawCardEvent on current player") {
      val game =
        GameStarted(
          aggregateId = GameId(),
          players = PlayerList(setOf(player1, player2)),
          drawPile = DrawPile(),
          discardPile = DiscardPile(),
          lastPlayerId = player1.id,
          nextPlayerId = player2.id,
          currentColor = Card.Color.Red,
          version = 1,
          recordedEvents = setOf(),
        )
      val card = Card.NumericCard(1, Card.Color.Blue)
      PlayerHaveDrawCardEvent(
        game.aggregateId,
        version = 2,
        playerId = player1.id,
        takenCards = setOf(card),
      ).toNotification(
        game = game,
        currentUserId = player1.userId,
      ).let {
        it.toList().size shouldBe 2
        it.toList().let { notifications ->
          assertInstanceOf<YourNewCardNotification>(notifications.first()).let {
            it.cards.first() shouldBe card
          }
          assertInstanceOf<ItsTheTurnOfNotification>(notifications[1]).let {
            it.player.id shouldBe player2.id
          }
        }
      }
    }
    test("PlayerHaveDrawCardEvent on other player") {
      val game =
        GameStarted(
          aggregateId = GameId(),
          players = PlayerList(setOf(player1, player2)),
          drawPile = DrawPile(),
          discardPile = DiscardPile(),
          lastPlayerId = player1.id,
          nextPlayerId = player2.id,
          currentColor = Card.Color.Red,
          version = 1,
          recordedEvents = setOf(),
        )
      val card = Card.NumericCard(1, Card.Color.Blue)
      PlayerHaveDrawCardEvent(
        game.aggregateId,
        version = 2,
        playerId = player1.id,
        takenCards = setOf(card),
      ).toNotification(
        game = game,
        currentUserId = player2.userId,
      ).let {
        it.toList().size shouldBe 2
        it.toList().let { notifications ->
          assertInstanceOf<PlayerHavePassNotification>(notifications.first()).let {
            it.playerId shouldBe player1.id
          }
          assertInstanceOf<ItsTheTurnOfNotification>(notifications[1]).let {
            it.player.id shouldBe player2.id
          }
        }
      }
    }

    test("CardIsPlayedEvent on current player") {
      val game =
        GameStarted(
          aggregateId = GameId(),
          players = PlayerList(setOf(player1, player2)),
          drawPile = DrawPile(),
          discardPile = DiscardPile(),
          lastPlayerId = player2.id,
          nextPlayerId = player1.id,
          currentColor = Card.Color.Red,
          version = 1,
          recordedEvents = setOf(),
        )
      val card = Card.NumericCard(1, Card.Color.Blue)
      CardIsPlayedEvent(
        game.aggregateId,
        version = 2,
        playerId = player1.id,
        card = card,
      ).toNotification(
        game = game,
        currentUserId = player1.userId,
      ).toList()
        .let { notifications ->
          notifications.size shouldBe 2
          assertInstanceOf<PlayerAsPlayACardNotification>(notifications.first()).let {
            it.playerId shouldBe player1.id
            it.card shouldBe card
          }
          assertInstanceOf<ItsTheTurnOfNotification>(notifications[1]).let {
            it.player.id shouldBe player1.id
          }
        }
    }
    test("CardIsPlayedEvent on other player") {
      val game =
        GameStarted(
          aggregateId = GameId(),
          players = PlayerList(setOf(player1, player2)),
          drawPile = DrawPile(),
          discardPile = DiscardPile(),
          lastPlayerId = player1.id,
          nextPlayerId = player2.id,
          currentColor = Card.Color.Red,
          version = 1,
          recordedEvents = setOf(),
        )
      val card = Card.NumericCard(1, Card.Color.Blue)
      CardIsPlayedEvent(
        game.aggregateId,
        version = 2,
        playerId = player2.id,
        card = card,
      ).toNotification(
        game = game,
        currentUserId = player1.userId,
      ).let {
        it.toList().size shouldBe 2
        it.toList().let { notifications ->
          assertInstanceOf<PlayerAsPlayACardNotification>(notifications.first()).let {
            it.playerId shouldBe player2.id
            it.card shouldBe card
          }
          assertInstanceOf<ItsTheTurnOfNotification>(notifications[1]).let {
            it.player.id shouldBe player2.id
          }
        }
      }
    }

    test("DrawFilledWithDiscardEvent") {
      val game =
        GameStarted(
          aggregateId = GameId(),
          players = PlayerList(setOf(player1, player2)),
          drawPile = DrawPile(),
          discardPile = DiscardPile(),
          lastPlayerId = player1.id,
          nextPlayerId = player2.id,
          currentColor = Card.Color.Red,
          version = 1,
          recordedEvents = setOf(),
        )
      DrawFilledWithDiscardEvent(
        game.aggregateId,
        version = 2,
        newDrawPile = DrawPile(),
        newDiscardPile = DiscardPile(),
      ).toNotification(
        game = game,
        currentUserId = player1.userId,
      ).let {
        it.toList().size shouldBe 1
        assertInstanceOf<PilesShuffledNotification>(it.first())
      }
    }

    test("GameStartedEvent") {
      val game =
        GameStarted(
          aggregateId = GameId(),
          players = PlayerList(setOf(player1, player2)),
          drawPile = DrawPile(),
          discardPile = DiscardPile(),
          lastPlayerId = player1.id,
          nextPlayerId = player2.id,
          currentColor = Card.Color.Red,
          version = 1,
          recordedEvents = setOf(),
        )
      GameStartedEvent(
        game.aggregateId,
        version = 2,
        firstPlayer = player1.id,
        playersHans =
          mapOf(
            player1.id to player1.hand,
            player2.id to player2.hand,
          ),
        drawPile = DrawPile(),
        discardPile = DiscardPile(),
      ).toNotification(
        game = game,
        currentUserId = player1.userId,
      ).toList()
        .let { notifications ->
          notifications.size shouldBe 2
          assertInstanceOf<TheGameWasStartedNotification>(notifications.first()).let {
            it.hand.size shouldBe 1
            it.hand.first() shouldBe player1.hand.cards.first()
          }
          assertInstanceOf<ItsTheTurnOfNotification>(notifications[1]).let {
            it.player.id shouldBe player2.id
          }
        }
    }
  })
