(function() {
	angular.module('webcompiler')
		.controller('CompilerController', ['templateService', 'compilationService', CompilerController] );

	function CompilerController(templateService, compilationService) {
		var vm = this;

		/// Initialization
		(function() {
			vm.template = 'c_basic.c';
			vm.working = false;
			vm.compilationOutput = "";
			vm.executionOutput = "";
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
			vm.compilationOutput = '';
			vm.executionOutput = '';
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
				compilationService.stopWatch();
				vm.compilationOutput = data.compilationOutput;
				compilationService.execute(
					function() {
						vm.step = 'run';
						compilationService.watchExecution(manageWorkflow);
					},
					function() {
						vm.step = '';
						vm.working = false;
					}
				)
			}
			else if (data.status == 'COMPILE_ERROR') {
				compilationService.stopWatch();
				vm.step = '';
				vm.working = false;
				vm.compilationOutput = data.compilationOutput;
			}
			else if (data.status == 'EXECUTED' || data.status == 'EXECUTION_ERROR') {
				compilationService.stopWatch();
				vm.step = '';
				vm.working = false;
				vm.executionOutput = data.executionOutput;
			}
		}
	}
})();
