const path = require('path');

module.exports = {
    entry: './target/js/main.js',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'bundle.js'
    }
};