root_dir=$(pwd)

docker build . -t luckyblock
docker run --rm \
    --volume $root_dir:/app \
    --volume $HOME/.gradle/caches:/root/.gradle/caches \
    --volume $HOME/.gradle/wrapper:/root/.gradle/wrapper \
    luckyblock
