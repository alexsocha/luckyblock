module.exports = {
    mode: 'production',
    name: 'luckyblock',
    entry: './src/main/resources/main.js',
    target: 'node',
    output: {
        path: __dirname + '/build/processedResources/js/main/addon/behavior_pack/scripts/server',
        filename: 'serverScript.js',
    },
}
