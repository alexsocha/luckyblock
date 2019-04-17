script_dir=$(dirname $0)
run_type=${1:-client}

# copy lucky block configuration
#mkdir -p run/config/lucky_block/version-
echo $version
#cp -rf lucky k

if [ "$run_type" = "client" ]
then
    #$script_dir/../gradlew runClient
    echo "hi"
else
    $script_dir/../gradlew runServer
fi
