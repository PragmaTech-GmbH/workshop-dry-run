# Workshop Dry Run - Verify Your Setup ✅

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/PragmaTech-GmbH/workshop-dry-run)

This is a small, self-contained Spring Boot project you run **before** a workshop with us to confirm your machine is ready.

It is a slimmed-down version of the workshop's bookshelf application. 

Its only job is to exercise the four tools we rely on during the workshop and tell you, per tool, whether your local setup works:

- **WireMock** - stubbing an external HTTP API
- **Docker + Testcontainers (PostgreSQL)** - a throwaway database
- **Docker + Testcontainers (Keycloak)** - a real OIDC issuer for JWT auth
- **Docker + Testcontainers (Mailpit)** - capturing outgoing e-mail

## Prerequisites

- **Java 25** (`java -version` should report 25)
- **Docker** running locally (`docker ps` should succeed) - required for Testcontainers
- An IDE (IntelliJ IDEA, VS Code, …) is helpful but not required
- No global Maven needed - the project ships with the Maven wrapper (`./mvnw`)

## The one command that proves everything

```bash
./mvnw verify
```

For Windows:

```bash
mvnw.cmd verify
```

If the build is **green**, your setup is ready ✅

The first run downloads dependencies and pulls Docker images, so it may take a few minutes. 

Node.js is downloaded automatically by the build to compile the small admin UI - you do not need it installed.

## What each test checks

`./mvnw verify` runs four integration tests. If one fails, it points at exactly one piece of your
setup:

| Test                       | Verifies                          | If it fails, check…                                  |
|----------------------------|-----------------------------------|------------------------------------------------------|
| `OpenLibraryWireMockIT`    | WireMock                          | Nothing Docker-related - a free local port for WireMock |
| `PostgresPersistenceIT`    | Docker + Testcontainers (Postgres) | Docker is running; `postgres:16-alpine` can be pulled |
| `KeycloakAuthenticationIT` | Docker + Testcontainers (Keycloak) | Docker is running; `quay.io/keycloak/keycloak:26.3` can be pulled |
| `MailNotificationIT`       | Docker + Testcontainers (Mailpit)  | Docker is running; `axllent/mailpit:v1.20` can be pulled |

A common first failure is **Docker not running** - all three container-backed tests fail with a
Docker connection error. Start Docker Desktop (or your daemon) and re-run.

## No local Docker? Use GitHub Codespaces

If you can't get Docker working locally, click the **Open in GitHub Codespaces** badge above (or *Code → Codespaces → Create codespace on main*). 

The container ships Java 25, Maven, and Docker-in-Docker, and automatically runs `./mvnw verify` on first start. See [`.devcontainer/README.md`](.devcontainer/README.md) for details.

## Running the app (optional)

You can also start the app and click around the small admin UI:

```bash
docker compose up -d     # starts Postgres, Keycloak, and Mailpit locally
./mvnw spring-boot:run
```

Then open <http://localhost:8080/app>. 

The included `compose.yaml` starts the same services the tests use (Postgres on 5432, Keycloak on 8090, Mailpit UI on 8025); Spring Boot's Docker Compose support wires them up automatically.

## Getting help

If `./mvnw verify` stays red after checking the table above, capture the full output and bring it to the workshop (or send it ahead) so we can sort it out before we start.
