FROM openjdk:16-jdk-slim

RUN apt-get update && apt-get install -y git

WORKDIR /app
COPY . .

CMD ["./gradlew", "build"]
