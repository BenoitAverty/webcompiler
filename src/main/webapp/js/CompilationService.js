(function() {
    angular.module('webcompiler')
        .factory('compilationService', ['$http', '$interval', CompilationService]);

    function CompilationService($http, $interval) {
        var serviceInstance = {
            compile: compile,
            execute: execute,
            watchCompilation: watchCompilation,
            watchExecution: watchExecution,
            stopWatch: stopWatch
        };
        return serviceInstance;

        /// Implementation
        var programId = 0;
        var executionId = 0;
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

        function execute(callback, error) {
            $http({
                method: 'POST',
                url: '/programs/'+programId+'/executions'
            }).then(function(response) {
                if(response.data.status == 'OK') {
                    executionId = response.data.executionId;
                    callback();
                }
                else {
                    error();
                }
            }, error);
        }

        function watchCompilation(callback) {
            if(watcher != null) {
                $interval.cancel(watcher);
            }

            watcher = $interval(function() {
                $http({
                    method: 'GET',
                    url: '/programs/'+programId+'/status',
                }).then(function(response) {
                    callback(response.data);
                });
            }, 1000);
        }

        function watchExecution(callback) {
            if(watcher != null) {
                $interval.cancel(watcher);
            }

            watcher = $interval(function() {
                $http({
                    method: 'GET',
                    url: '/executions/'+executionId+'/status',
                }).then(function(response) {
                    callback(response.data);
                });
            }, 1000);
        }

        function stopWatch() {
            $interval.cancel(watcher);
            watcher = null;
        }
    }
})();
