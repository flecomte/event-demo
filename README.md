Event demo
==========
- [Installation](#installation)
- [What's in this demo](#whats-in-this-demo)


Installation
------------

To run the stack:

```shell
docker compose -f docker\docker-compose.yaml -p event-demo up -d
```

Admin service URL:
 - [Træfik](http://pgadmin.traefik.me/)
 - [Redis](http://pgadmin.traefik.me/)
 - [pgAdmin](http://pgadmin.traefik.me/)
 - [API](http://api.traefik.me/)

What's in this demo
-------------------

- The **event sourcing** pattern.
- The **event driven** pattern.
- The **CQRS** pattern with **command** and **query**.
- A fully **asynchronous** architecture.Concurently process.
- A **pure Kotlin** implementation of **readmodel**/**projection**.
- A **Redis** implementation of **readmodel**/**projection**.
- A **pure Kotlin** implementation of **Event Store**.
- A **Postgresql** implementation of **Event Store**.
- A **pure Kotlin** implementation of **Event Bus**.
- A **RabbitMQ** implementation of **Event Bus**.
- A **Hexagonal** architecture.
- Use of **Web Sockets**.
- Use of the classic **Rest** route.
- Simple usage of the **JWT**.
- The **Ktor** framework.
- The **Koin** Dependency Injection framework
- Concurrently process.
- Use of coroutines.
- Using **docker compose** for the stack with **traefik**.
- Use of **flyway** to migrate the postgresql schema.

The stack
---------

- Kotlin
- Ktor
- Postgresql
- Redis
- RabbitMQ
- Docker
- Træfik
- Flyway