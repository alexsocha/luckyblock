script_dir=$(dirname $0)
dist_dir=$script_dir/../build/dist
deploy_dir=/root/luckyblock-dist

find build/dist/* -maxdepth 1 -type d -exec \
    scp -r {} root@luckyblockmod.com:$deploy_dir \;
