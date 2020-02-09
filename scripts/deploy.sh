script_dir=$(dirname $0)
dist_dir=$script_dir/../build/dist
secret_folder=jKEbJUId
deploy_dir=/var/www/html/projects/lucky_block/download/version/files/$secret_folder

find $dist_dir -regex ".*\.jar" -exec \
  scp {} root@minecraftascending.com:$deploy_dir \;
