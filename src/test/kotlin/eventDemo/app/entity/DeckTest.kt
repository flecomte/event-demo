package eventDemo.app.entity

import eventDemo.allCardCount
import eventDemo.allCards
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.ints.shouldBeExactly
import kotlin.test.assertNotNull

class DeckTest :
    FunSpec({
        val totalCardsNumber = 104
        test("newWithoutPlayers") {
            // When
            val deck = Deck.newWithoutPlayers()

            // Then
            deck.stack.size shouldBeExactly totalCardsNumber
            deck.discard.size shouldBeExactly 0
            deck.playersHands.size shouldBeExactly 0

            deck.allCardCount() shouldBeExactly totalCardsNumber
            deck.allCards().shouldBeUnique()
            deck.allCards().map { it.id }.shouldBeUnique()
        }

        test("initHands") {
            // Given
            val playerNumbers = 4
            val players = (1..playerNumbers).map { Player(name = "name $it") }.toSet()
            val deck = Deck.newWithoutPlayers()

            // When
            val initDeck = deck.initHands(players)

            // Then
            initDeck.stack.size shouldBeExactly totalCardsNumber - (playerNumbers * 7)
            initDeck.discard.size shouldBeExactly 0
            initDeck.playersHands.size shouldBeExactly playerNumbers
            initDeck.playersHands.forEach { (_, cards) -> cards.size shouldBeExactly 7 }
            initDeck.allCardCount() shouldBeExactly totalCardsNumber
        }

        test("takeOneCardFromStackTo") {
            // Given
            val playerNumbers = 4
            val players = (1..playerNumbers).map { Player(name = "name $it") }.toSet()
            val deck = Deck.newWithoutPlayers().initHands(players)
            val firstPlayer = players.first()

            // When
            val modifiedDeck = deck.takeOneCardFromStackTo(firstPlayer)

            // Then
            modifiedDeck.discard.size shouldBeExactly 0
            modifiedDeck.stack.size shouldBeExactly totalCardsNumber - (playerNumbers * 7) - 1
            modifiedDeck.playersHands.size shouldBeExactly playerNumbers
            assertNotNull(modifiedDeck.playersHands.getHand(firstPlayer)).size shouldBeExactly 7 + 1
            modifiedDeck.playersHands
                .filterKeys { it != firstPlayer.id }
                .forEach { (_, cards) -> cards.size shouldBeExactly 7 }
            modifiedDeck.allCardCount() shouldBeExactly totalCardsNumber
        }

        test("putOneCardFromHand") {
            // Given
            val playerNumbers = 4
            val players = (1..playerNumbers).map { Player(name = "name $it") }.toSet()
            val deck = Deck.newWithoutPlayers().initHands(players)
            val firstPlayer = players.first()

            // When
            val card = deck.playersHands.getHand(firstPlayer)!!.first()
            val modifiedDeck = deck.putOneCardFromHand(firstPlayer, card)

            // Then
            modifiedDeck.discard.size shouldBeExactly 1
            modifiedDeck.stack.size shouldBeExactly totalCardsNumber - (playerNumbers * 7)
            modifiedDeck.playersHands.size shouldBeExactly playerNumbers
            assertNotNull(modifiedDeck.playersHands.getHand(firstPlayer)).size shouldBeExactly 6
            modifiedDeck.playersHands
                .filterKeys { it != firstPlayer.id }
                .forEach { (_, cards) -> cards.size shouldBeExactly 7 }
            modifiedDeck.allCardCount() shouldBeExactly totalCardsNumber
        }

        test("placeFirstCardOnDiscard") {
            // Given
            val playerNumbers = 4
            val players = (1..playerNumbers).map { Player(name = "name $it") }.toSet()
            val deck = Deck.newWithoutPlayers().initHands(players)
            val firstPlayer = players.first()

            // When
            val modifiedDeck = deck.placeFirstCardOnDiscard()

            // Then
            modifiedDeck.discard.size shouldBeExactly 1
            modifiedDeck.stack.size shouldBeExactly totalCardsNumber - (playerNumbers * 7) - 1
            modifiedDeck.playersHands.size shouldBeExactly playerNumbers
            modifiedDeck.playersHands
                .forEach { (_, cards) -> cards.size shouldBeExactly 7 }
            modifiedDeck.allCardCount() shouldBeExactly totalCardsNumber
        }
    })
