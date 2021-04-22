FROM openjdk:8

WORKDIR /app
COPY . .

CMD ["./gradlew", "build"]
