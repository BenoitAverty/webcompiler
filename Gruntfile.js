module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    wiredep: {
      app: {
        src: ['src/main/webapp/index.html'],
      },
      test: {
        src: ['src/test/javascript/karma.conf.js'],
        devDependencies: true,
        ignorePath: /\.\.\/\.\.\//, // remove ../../ from paths of injected javascripts
        fileTypes: {
          js: {
            block: /(([\s\t]*)\/\/\s*bower:*(\S*))(\n|\r|.)*?(\/\/\s*endbower)/gi,
              detect: {
                js: /'(.*\.js)'/gi
              },
              replace: {
                js: '\'{{filePath}}\','
              }
          }
        }
      }
    },
    karma: {
      unit: {
        configFile: 'src/test/javascript/karma.conf.js',
        singleRun: 'true'
      }
    }
  });

  grunt.loadNpmTasks('grunt-wiredep');
  grunt.loadNpmTasks('grunt-karma');

  // Default task(s).
  grunt.registerTask('build', ['wiredep:app']);

  grunt.registerTask('test', ['wiredep:test','karma']);

};
