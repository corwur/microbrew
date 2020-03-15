module.exports = {
    files: {
        javascripts: {
            joinTo: {
                'app.js' : 'app/**/*.js',
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
    modules: {
        autoRequire: {
            'app.js' : ['application']
        }
    },
    paths: {
        watched: ["app", "src" ]
    },
    plugins: {
        babel: {presets: ['latest']}
    },
    npm: {
        styles: {
        	'bootstrap': ['dist/css/bootstrap.css'],  
        	'tabulator-tables': ['dist/css/tabulator.min.css'],
        	"cytoscape-context-menus": ['cytoscape-context-menus.css']},
        
    }
};