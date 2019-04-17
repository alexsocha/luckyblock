script_dir=$(dirname $0)
dist_dir=$script_dir/../build/libs
secret_folder=jKEbJUId
deploy_dir=/var/www/html/projects/lucky_block/download/version/files/$secret_folder

find $dist_dir -regex ".*/luckyblock-.*[0-9]-.*[0-9]\.zip" -exec \
  scp {} root@minecraftascending.com:$deploy_dir \;
