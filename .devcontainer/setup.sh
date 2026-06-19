#!/bin/bash
set -e

echo "Setting up the workshop dry-run environment..."

# Configure Git
git config --global pull.rebase true
git config --global core.autocrlf input

echo "Pulling Docker images used by the setup-verification tests..."
docker pull postgres:16-alpine
docker pull axllent/mailpit:v1.20
docker pull quay.io/keycloak/keycloak:26.3
docker pull testcontainers/ryuk:0.13.0

# Add execution permission to the Maven wrapper
echo "Making Maven wrapper executable..."
chmod +x ./mvnw

# Build + run the verification tests to warm the dependency cache and confirm the setup
echo "Building project and running setup-verification tests..."
./mvnw verify

echo "Setup complete! If the build above is green, your environment is ready for the workshop."
