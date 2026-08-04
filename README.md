Event Demo
==========
- [Installation](./doc/installation.md)
- [What's the demo for ?](#whats-the-demo-for-)
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

The stack
---------

Language
- Kotlin

Framework
- Ktor
  - with Koin for Dependency Injection

Database
- Postgresql
  - with Flyway
- Redis
- RabbitMQ

Infra
- Docker
- Træfik
