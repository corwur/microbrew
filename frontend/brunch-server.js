
const express = require('express');
const httpProxy = require('http-proxy');

const app = express();
const proxy = httpProxy.createProxyServer({});

function gateway() {
    return function(req,res,next) {
        proxy.web(req, res,{ target: 'http://localhost:8090' })
    }
}

app.use(express.static(__dirname + '/public'));
app.use(gateway());

module.exports = (config, callback) => {
    app.listen(config.port, function () {
        console.log(`Listening on port ${config.port}!`);
        callback();
    });
    return app;
};