docker build . -t luckyblock-docs
container_id=$(docker create luckyblock-docs)
docker cp $container_id:/app/dist/. ./dist/
