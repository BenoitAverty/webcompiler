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
        devDependencies: true
      }
    },
    karma: {
      unit: {
        configFile: 'src/test/javascript/karma.conf.js',
        singleRun: 'true'
      }
    }
  });

  // Load the plugin that provides the "wiredep" task.
  grunt.loadNpmTasks('grunt-wiredep');
  grunt.loadNpmTasks('karma');

  // Default task(s).
  grunt.registerTask('build', ['wiredep:app']);

  grunt.registerTask('test', ['wiredep:test','karma']);

};
