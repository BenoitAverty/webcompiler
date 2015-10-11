// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

module.exports = function (config) {
    config.set({
        // base path, that will be used to resolve files and exclude
        basePath: '../../',

        // testing framework to use (jasmine/mocha/qunit/...)
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            // bower:js
            'main/webapp/bower_components/jquery/dist/jquery.js',
            'main/webapp/bower_components/angular/angular.js',
            'main/webapp/bower_components/angular-cache-buster/angular-cache-buster.js',
            'main/webapp/bower_components/bootstrap/dist/js/bootstrap.js',
            'main/webapp/bower_components/angular-mocks/angular-mocks.js',
            'main/webapp/bower_components/angular-scenario/angular-scenario.js',
            // endbower
            'main/webapp/js/webcompiler.module.js',
            'main/webapp/js/**/*.js',
            'test/javascript/**/!(karma.conf).js'
        ],


        browsers: ['PhantomJS']
    });
};
