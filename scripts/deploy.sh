script_dir=$(dirname $0)
dist_dir=$script_dir/../dist
deploy_dir=/root/luckyblock-dist

find $dist_dir/* -maxdepth 1 -type d -exec \
    scp -r {} root@luckyblockmod.com:$deploy_dir \;
