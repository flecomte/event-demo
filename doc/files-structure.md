# Exemple de structure 

Les couches, du plus interne au plus externe
```
Domain (le cœur, ne dépend de RIEN d'externe)
↑
Application (orchestre le Domain, ne connaît pas l'infra concrète)
↑
Infrastructure (WebSocket, DB, event store — dépend de tout le reste)
```

```
src/
└── contexts/
    ├── auth/
        └── ... 
    └── game/
        ├── domain/                              ← Le cœur métier, zéro dépendance externe
        │   ├── game/
        │   │   ├── Game.ts                      ← Aggregate Root
        │   │   ├── Player.ts                    ← Entity interne
        │   │   ├── Card.ts                      ← Entity
        │   │   ├── Color.ts                     ← Value Object
        │   │   ├── Deck.ts                      ← VO ou petite structure
        │   │   └── errors/
        │   │       ├── InvalidMoveError.ts
        │   │       └── ColorChoiceRequiredError.ts
        │   └── events/                          ← Events de DOMAINE (internes)
        │       ├── CardPlayed.ts
        │       ├── CardDrawn.ts
        │       ├── TurnPassed.ts
        │       └── DomainEvent.ts               ← interface/type de base
        ├── application/                         ← Orchestration, cas d'usage
        │   ├── commands/                        ← Les Commandes (intentions)
        │   │   ├── PlayCardCommand.ts
        │   │   └── DrawCardCommand.ts
        │   ├── handlers/                        ← Un handler par commande
        │   │   ├── PlayCardHandler.ts           ← charge l'aggregate, appelle game.playCard(), save
        │   │   └── DrawCardHandler.ts
        │   ├── projections/                     ← LA LOGIQUE de construction des projections
        │   │   ├── GameSummaryProjector.kt      ← écoute les events, met à jour la vue
        │   │   └── PlayerStatsProjector.kt
        │   └── ports/                           ← INTERFACES seulement (le "hexagone")
        │       ├── GameRepository.ts            ← interface, pas d'implémentation
        │       ├── EventPublisher.ts            ← interface, pas d'implémentation
        │       └── ProjectionStore.kt           ← interface, où lire/écrire la projection
        ├── infrastructure/                      ← Tout ce qui est technique/externe
        │   ├── persistence/
        │   │   ├── EventStoreGameRepository.ts  ← implémente GameRepository
        │   │   ├── EventStore.ts
        │   │   ├── projections/
        │   │   │   ├── GameSummaryProjectionStore.kt   ← implémentation concrète (DB, table dédiée)
        │   │   │   └── models/
        │   │   │       └── GameSummaryView.kt          ← structure de la vue elle-même 
        │   ├── websocket/
        │   │   ├── WebSocketServer.ts
        │   │   ├── connectionManager.ts         ← Map<gameId, Map<playerId, WebSocket>>
        │   │   └── commandRouter.ts             ← reçoit le message brut, dispatch vers le bon handler
        │   └── eventPublisher/
        │       └── WebSocketEventPublisher.ts   ← implémente EventPublisher, fait le broadcast
        └── presentation/                        ← Traduction vers/depuis le client (le fameux DTO layer)
            ├── clientEvents/
            │   ├── ClientEvent.ts               ← types des events envoyés au front
            │   └── toClientEvent.ts             ← fonction de traduction domain event → client event
            └── clientCommands/
                └── parseIncomingCommand.ts      ← valide/parse le message brut du client → Command
```