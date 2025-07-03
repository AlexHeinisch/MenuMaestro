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
mvn -f application/pom.xml spring-boot:run
```

Skip Tests

```sh
mvn clean install -DskipUnitTests
mvn clean install -DskipIntegrationTests
mvn clean install -DskipTests
```