FROM openjdk:8-jdk-slim

RUN apt-get update && apt-get install -y git

WORKDIR /app
COPY . .

CMD ["bash", "-c", "./gradlew build"]
