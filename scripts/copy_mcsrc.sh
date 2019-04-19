mcsrc_dir=$(dirname $0)/../_mcsrc

rm -rf $mcsrc_dir
mkdir $mcsrc_dir

find ~/.gradle/caches -wholename "*/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/*_mapped*/forge-*-sources.jar" \
    -exec cp {} $mcsrc_dir/src.jar \; -quit

cd $mcsrc_dir
jar xf src.jar
rm src.jar
