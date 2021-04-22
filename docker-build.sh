root_dir=$(pwd)

docker run --rm \
    --volume $root_dir:/app \
    --volume $HOME/.gradle/caches:/root/.gradle/caches \
    --volume $HOME/.gradle/wrapper:/root/.gradle/wrapper \
    openjdk:8 \
    bash -c "cd /app && ./gradlew :fabric:build"
