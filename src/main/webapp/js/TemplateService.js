(function() {
    angular.module('webcompiler')
        .factory('TemplateService', ['$http', TemplateService]);

    function TemplateService($http) {
        var serviceInstance = {
            get: get
        };
        return serviceInstance;

        /// Implementation
        function get(code, callback) {
            $http({
                method: 'GET',
                url: 'templates/'+code
            }).then(function(response) {
                callback(response.data);
            });
        }
    }
})();
