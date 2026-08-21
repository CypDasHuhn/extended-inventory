set shell := ["bash", "-uc"]

default:
    @just --list

# Build the plugin jar
build:
    ./gradlew shadowJar

# Run a Paper server with the plugin
run:
    ./gradlew runServer

# Run tests
test:
    ./gradlew test

# Clean build outputs
clean:
    ./gradlew clean
