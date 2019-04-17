mcsrc_dir=$(dirname $0)/../_mcsrc

rm -rf $mcsrc_dir
mkdir $mcsrc_dir

find ~/.gradle/caches -wholename "*/minecraft/net/minecraftforge/forge/*/forgeSrc-*-sources.jar" \
    -exec cp {} $mcsrc_dir/src.jar \; -quit

cd $mcsrc_dir
jar xf src.jar
