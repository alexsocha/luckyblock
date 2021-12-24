docker build . -t luckyblock
container_id=$(docker create luckyblock)
docker cp $container_id:/app/dist/. ./dist/
