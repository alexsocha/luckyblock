#
# Base
#
FROM openjdk:16-jdk-slim as base

RUN apt-get update && apt-get install -y git

#
# Test
#
FROM base as test

WORKDIR /app
COPY . .

RUN ./gradlew :tools:test

#
# Build
#
FROM base as build

CMD ["bash", "-c", "./gradlew build"]
