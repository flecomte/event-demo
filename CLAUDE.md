# CLAUDE.md — event-demo

Ce fichier donne le contexte du projet pour toute session Claude Code future sur ce dépôt.

## Vue d'ensemble

`event-demo` est un projet démo personnel (Fabrice Lecomte) qui illustre plusieurs patterns
d'architecture backend :

- Event Sourcing
- Event-Driven (bus d'événements asynchrone)
- CQRS (séparation commandes / projections en lecture)
- Architecture Hexagonale (ports & adapters), un dossier par *bounded context*

Le cas d'usage servant de support est un jeu de cartes façon UNO (créer une partie, rejoindre,
jouer une carte, piocher, etc.), avec authentification des joueurs.

Dépôts distants configurés : `gitea` (auto-hébergé, git.gogn.synology.me — remote historique)
et `github` (`flecomte/event-demo`, miroir). Vérifier vers lequel pousser selon le contexte.

## Stack technique

- **Langage** : Kotlin 2.1.21, JDK 21 (toolchain Gradle)
- **Framework serveur** : Ktor 3.5.1 (Netty), DI via Koin 4.2.1
- **Sérialisation** : kotlinx.serialization (JSON)
- **Persistance** :
  - PostgreSQL (event store, via HikariCP) + migrations Flyway (`migrations/events/`)
  - RabbitMQ (bus d'événements / bus de commandes, via amqp-client)
- **Auth** : JWT (ktor-server-auth-jwt), hash de mot de passe via password4j
- **Infra dev/prod** : Docker Compose (fichiers `docker/docker-compose-{dev,test,prod}.yaml`
  incluant des « parts » réutilisables dans `docker/parts/`), reverse proxy Træfik
- **Tests** : Kotest (runner JUnit5), MockK, kotest-extensions-koin, ArchUnit (test d'architecture)
- **Qualité** : ktlint (`ktlint_official`, standard + experimental activés), reporting checkstyle
- **CI** : GitHub Actions (`.github/workflows/tests.yml`) — build/cache Gradle, `ktlintCheck`,
  puis tests exécutés **dans Docker** (`docker compose -f docker/docker-compose-test.yaml run tests`)
- **API** : documentée en OpenAPI (`resources/openapi/documentation.yaml`)

## Architecture

Un dossier par *bounded context* sous `src/main/kotlin/eventDemo/contexts/<context>/`, chacun
strictement découpé en 3 couches :

- `domain/` — aucune dépendance vers les autres couches
- `application/` — ne dépend que de `domain`
- `infrastructure/` — dépend de `domain` et `application`

Contexts actuels :

- **`auth`** : `User`, création de compte, login JWT, event store dédié (Postgresql),
  projection utilisateur.
- **`game`** : cœur du jeu — `Card`, `DrawPile`/`DiscardPile`, `Player`, `GameId`, commandes
  (`JoinTheGameCommand`, `PlayCardCommand`, `ReadyToPlayCommand`, `TakeCartFromDrawPileCommand`),
  state machine du jeu via `sealed interface Game` (`GameInit` → `GameCreated` → `GameStarted` →
  `GameEnded`), notifications, projections (liste de parties), listeners/réactions.

Libs transverses dans `libs/` (indépendantes de tout contexte) :

- `bus/` — abstraction `Bus<E>` avec implémentations in-memory et RabbitMQ (fanout exchange)
- `command/` — `Command`, `CommandUnicityChecker` (empêche la double exécution d'une commande,
  cache glissant de 10 min par défaut)
- `eventSource/` — `Event`, `EventStream` (append/lecture par version, gestion de
  `VersionConflictException`), `EventStore` in-memory / Postgresql
- `helpers/`, `serializer/` — utilitaires (conversion de frames WebSocket, sérialiseurs UUID, etc.)

## Patterns notables dans le code

- **Event sourcing** : `Game.loadFromHistory(events)` reconstruit l'état en repliant
  (`fold`) les événements sur une state machine scellée, en utilisant la syntaxe Kotlin 2.1
  `when` avec garde `if` (ex. `is GameCreatedEvent if this is GameInit -> applyEvent(event)`).
- **CQRS** : écriture via les command handlers (`application/command/handlers`), lecture via des
  projections dédiées (`application/projections`), propagées via le bus RabbitMQ, pas de couplage
  direct avec l'écriture.
- **Event-driven** : réactions asynchrones (`ReactionListener`, `EventToNotificationSubscriber`)
  déclenchées par le bus RabbitMQ (exchange fanout, une queue par abonné).
- **Exceptions métier** : hiérarchie `GameException` / `IllegalActionException` dans
  `domain/game/errors`, une exception par règle métier violée (ex.
  `NeedMorePlayersToStartGameException`, `ItsNotTheTurnException`).

## Commandes utiles

```shell
./gradlew build            # build complet
./gradlew test             # tests (JUnit5 via Kotest)
./gradlew ktlintCheck      # lint
./gradlew ktlintFormat     # auto-format
./gradlew buildFatJar      # jar exécutable "all-in-one" (utilisé par le Dockerfile prod)

# Dépendances seules (Postgres, RabbitMQ, Træfik, pgAdmin...) pour lancer l'app en local hors docker
docker compose -f docker/docker-compose-dev.yaml up -d

# Stack de test façon CI
docker compose -f docker/docker-compose-test.yaml up -d
# ou directement (comme en CI) :
docker compose -f docker/docker-compose-test.yaml run tests

# Stack complète en prod
docker compose -f docker/docker-compose-prod.yaml -p event-demo up -d
```

URLs en dev (voir `doc/installation.md`, nécessite Træfik + résolution des `*.traefik.me`) :
API sur `http://api.traefik.me/`, dashboard
Træfik, pgAdmin et RabbitMQ management exposés via des sous-domaines `traefik.me`.

## Conventions de code

- ktlint en mode `ktlint_official` + règles `standard` et `experimental` activées
  (voir `.editorconfig`), indentation **2 espaces**, virgules finales (*trailing commas*)
  systématiques, wrapping forcé des expressions/signatures multi-lignes.
- Fins de ligne forcées en **LF** (`.gitattributes`), sauf `gradlew.bat` en CRLF.
- Code et identifiants en anglais.
- Style Kotlin idiomatique/fonctionnel : `fold`, `let`, `apply`, `when` exhaustifs, classes/interfaces
  scellées (`sealed class`/`sealed interface`) pour modéliser états et événements plutôt que des enums
  avec des champs optionnels.

## Pièges connus / choses à savoir avant de toucher au build ou à la CI

- **MockK/ByteBuddy en Docker** : l'auto-attach dynamique de MockK échoue dans les conteneurs
  (le handshake SIGQUIT de l'AttachListener JVM time-out). Le `build.gradle.kts` charge donc
  l'agent `byte-buddy-agent` de façon statique via `-javaagent` pour les tâches `Test`, afin
  que MockK détecte l'instrumentation déjà présente et saute l'attach dynamique. Ne pas retirer
  ce bloc sans repenser l'exécution des tests en Docker.
- **Secret Postgres en CI** : `docker/postgresql.secret` est généré à la volée par le workflow
  GitHub Actions s'il n'existe pas (`echo -n "changeit" > docker/postgresql.secret`) — normal,
  pas un fichier à committer.
- Les tests « officiels » de la CI tournent **dans Docker**, pas directement via `./gradlew test`
  sur l'hôte — en cas de comportement différent entre local et CI, vérifier d'abord les
  variables d'environnement/versions du `docker-compose-test.yaml`.

## Historique récent (pour contexte)

Le projet a connu un « Massive refactor to build the V2 » (commit `e2d7942`) : passage d'une
architecture par couches techniques plates (`adapter/presenter/domain`) à l'organisation actuelle
par bounded context (`auth`/`game`) avec 3 couches hexagonales chacune.
