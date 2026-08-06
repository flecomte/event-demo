Installation
============

To run the stack in production:

```shell
docker compose -f docker\docker-compose-prod.yaml -p event-demo up -d
```

To run only the app dependencies in development mode and run the app localy (not in docker):

```shell
docker compose -f docker\docker-compose-dev.yaml up -d
```

To run the tests in docker (it's designed for the CI):

```shell
docker compose -f docker\docker-compose-test.yaml up -d
```

Api url:
- [Backend API](http://api.traefik.me/)
- [Frontend web site](http://app.traefik.me/)

Exposed url on dev env:
- [PostgreSql](http://localhost:5432/)
- [RabbitMQ](http://localhost:15672/)

Admin service URL:
- [Træfik dashboard](http://traefik.traefik.me/)
- [pgAdmin](http://pgadmin.postgresql.traefik.me/)
- [RabbitMQ management](http://management.rabbitmq.traefik.me/)