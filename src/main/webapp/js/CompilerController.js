(function() {
	angular.module('webcompiler')
		.controller('CompilerController', ['templateService', 'compilationService', CompilerController] );

	function CompilerController(templateService, compilationService) {
		var vm = this;

		/// Initialization
		(function() {
			vm.template = 'c_basic.c';
			vm.working = false;
			vm.compilationResult = "";
			vm.executionResult = "";
			vm.step = "";
			loadTemplate();
		})();

		/// Public members
		vm.loadTemplate = loadTemplate;
		vm.triggerWorkflow = triggerWorkflow;

		/// Implementation
		function loadTemplate() {
			templateService.get(vm.template, function(source) {
				vm.sourcecode = source;
			});
		}

		function triggerWorkflow() {
			vm.working = true;
			compilationService.compile(vm.sourcecode,
				function() {
					vm.step = 'compile';
					compilationService.watchCompilation(manageWorkflow);
				},
				function() {
					vm.working = false;
				}
			);
		}

		function manageWorkflow(data) {
			if (data.status == 'COMPILED') {
				compilationService.stopWatchCompilation();
				vm.step = 'run';
				vm.compilationResult = data.compilationOutput;
			}
			else if (data.status == 'COMPILE_ERROR') {
				compilationService.stopWatchCompilation();
				vm.step = '';
				vm.working = false;
				vm.compilationResult = data.compilationOutput;
			}
		}
	}
})();
