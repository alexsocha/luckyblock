#
# Base
#
FROM openjdk:17-jdk-slim as base

RUN apt-get update && apt-get install -y git

# setup gradle
WORKDIR /app
COPY . .
RUN ./gradlew tasks

#
# Test
#
FROM base as test

WORKDIR /app
COPY . .

RUN ./gradlew :common:jvmTest
RUN ./gradlew :tools:test

#
# Build
#
FROM base as build

WORKDIR /app
COPY . .
RUN ./gradlew :forge:build
RUN ./gradlew :fabric:build
RUN ./gradlew :bedrock:build
