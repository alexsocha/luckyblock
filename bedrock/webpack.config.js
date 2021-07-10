module.exports = {
    mode: 'production',
    name: 'luckyblock',
    entry: './js-src/main.js',
    target: 'node',
    output: {
        path: __dirname + '/dist',
        filename: 'luckyblock.js',
    }
}
