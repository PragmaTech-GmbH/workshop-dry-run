# Development Container for the Workshop Dry Run

This directory configures a development container for use with **GitHub Codespaces** or local
development with **VS Code Dev Containers**. It is the zero-local-install fallback for verifying your
workshop setup: everything (Java, Maven, Docker) is provided inside the container.

## Features

- Java 25 (OpenJDK)
- Maven 3.9.12
- Docker-in-Docker (required for Testcontainers: PostgreSQL, Keycloak, Mailpit)
- VS Code extensions for Spring Boot development

## Getting Started

### Using GitHub Codespaces

1. On the GitHub repository page, click the green **Code** button.
2. Select the **Codespaces** tab.
3. Click **Create codespace on main**.
4. Wait for the codespace to start and `postCreateCommand` (`setup.sh`) to finish — it pulls the
   Docker images and runs `./mvnw verify`.

### Using VS Code Dev Containers

1. Install the [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
   extension.
2. Clone the repository and open it in VS Code.
3. When prompted, choose **Reopen in Container** (or run *Dev Containers: Reopen in Container*).

## Post-Setup Actions

After the container is created, `setup.sh` automatically:

1. Configures Git.
2. Pulls the required Docker images (`postgres:16-alpine`, `axllent/mailpit:v1.20`,
   `quay.io/keycloak/keycloak:26.3`, `testcontainers/ryuk:0.13.0`).
3. Makes the Maven wrapper executable.
4. Runs `./mvnw verify` to download dependencies and confirm the setup is green.

## Troubleshooting

1. Check the terminal output during container creation for errors.
2. Re-run the setup manually: `bash .devcontainer/setup.sh`.
3. Confirm Docker works inside the container: `docker ps`.
