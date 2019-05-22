module.exports = {
    files: {
        javascripts: {
            joinTo: {
                'app.js' : 'app/*.js',
                'js/vendor.js': /^node_modules/

            },
        },
        stylesheets: {
            joinTo: {
                'css/main.min.css': 'app/styles/main.scss',
                'css/app.css': 'app/styles/*.css',
                'css/vendor.css': /^node_modules/
            }
        }
    },
    paths: {
        watched: ["app", "src" ]
    },
    plugins: {
        elmBrunch: {
            mainModules: ["src/Main.elm"]
        },
        babel: {presets: ['latest']}
    },
    npm: {
        styles: {bootstrap: ['dist/css/bootstrap.css']},
    }
};