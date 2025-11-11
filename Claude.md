# Project Overview

This is a Spring Boot application with an Angular frontend integrated into the build process. The Angular app is built and served as static resources by Spring Boot.

## Architecture

- **Backend**: Spring Boot REST API
- **Frontend**: Angular SPA (Single Page Application)
- **Build**: Maven builds Angular during package phase
- **Deployment**: Single JAR with embedded frontend

## Commands

### Building the application with tests
```bash
mvn clean install
```

### Building the application without tests
```bash
mvn clean install -DskipTests
```

### Running the application

- build the application before running it to make sure you run the newest version
- start a postgres instance via docker

```bash
mvn spring-boot:run -pl application
```

Frontend runs under: localhost:8080/ui

### Branch Naming
```
feature/TICKET-123-description
```