#
# Base
#
FROM openjdk:17-jdk-slim as base

RUN apt-get update && apt-get install -y git

# setup gradle
WORKDIR /app

COPY ./gradlew ./gradlew
COPY ./gradle ./gradle

RUN ./gradlew -v

COPY ./gradle.properties ./gradle.properties
COPY ./settings.gradle.kts ./settings.gradle.kts
COPY ./common/build.gradle.kts ./common/build.gradle.kts
COPY ./common/src/jvmMain/resources/template-addons/versions.json \
     ./common/src/jvmMain/resources/template-addons/versions.json
COPY ./tools/build.gradle.kts ./tools/build.gradle.kts
COPY ./forge/build.gradle.kts ./forge/build.gradle.kts
COPY ./fabric/build.gradle.kts ./fabric/build.gradle.kts
COPY ./bedrock/build.gradle.kts ./bedrock/build.gradle.kts

RUN ./gradlew tasks

#
# Test
#
FROM base as test

WORKDIR /app
COPY . .

RUN ./gradlew :common:jvmTest --info
RUN ./gradlew :tools:test --info

#
# Build
#
FROM base as build

WORKDIR /app
COPY . .
RUN ./gradlew :forge:build
RUN ./gradlew :fabric:build
RUN ./gradlew :bedrock:build
