(function() {
    angular.module('webcompiler')
        .factory('compilationService', ['$http', '$interval', CompilationService]);

    function CompilationService($http, $interval) {
        var serviceInstance = {
            compile: compile,
            watchCompilation: watchCompilation,
            stopWatchCompilation: stopWatchCompilation
        };
        return serviceInstance;

        /// Implementation
        var programId = 0;
        var watcher = null;

        function compile(sourcecode, callback, error) {
            $http({
                method: 'POST',
                url: '/programs',
                data: sourcecode
            }).then(function(response) {
                if(response.data.status == 'OK') {
                    programId = response.data.programId;
                    callback();
                }
                else {
                    error();
                }
            }, error);
        }

        function watchCompilation(callback) {
            watcher = $interval(function() {
                $http({
                    method: 'GET',
                    url: '/programs/'+programId+'/status'
                }).then(function(response) {
                    callback(response.data);
                });
            }, 1000);
        }

        function stopWatchCompilation() {
            $interval.cancel(watcher);
        }
    }
})();
