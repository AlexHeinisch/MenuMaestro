# MenuMaestro Main Backend

## Quickstart

Start a postgres database (e.g. via docker-compose)
Setup the application properties accordingly

```sh
cd ../
cd infrastructure
docker compose up -d
```

Start Application

```sh
mvn clean install
mvn spring-boot:run -pl application
```

Skip Tests

```sh
mvn clean install -DskipUnitTests
mvn clean install -DskipIntegrationTests
mvn clean install -DskipTests
```

## End-to-End Tests

A Playwright suite covering the core user journeys (login, recipes, menus, meals,
shopping lists) lives in [`e2e/`](e2e/README.md), run against the full stack (Postgres +
backend + built Angular UI). See [`e2e/README.md`](e2e/README.md) for setup and how to
run it locally; CI runs it on every push to `main` via
[`.github/workflows/e2e.yml`](.github/workflows/e2e.yml).