Event Demo
==========
- [Installation](./doc/installation.md)
- [What's the demo for ?](#whats-the-demo-for-)
- [What's in this demo](#whats-in-this-demo)
- [The stack](#the-stack)
- [Architecture](./doc/architecture.md)


What's the demo for ?
--------------------

This demo is intended to demonstrate the implementation 
of different patterns and architectures.

- The Event sourcing pattern.
- The Event driver pattern.
- The CQRS pattern.
- With the Hexagonal architecture.


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

Language
- Kotlin

Framework
- Ktor

Database
- Postgresql
  - with Flyway
- Redis
- RabbitMQ

Infra
- Docker
- Træfik
