angular.module('webcompiler', [])
	.controller('CompilerController', function($scope) {
		$scope.sourcecode = "\
#include <stdio.h>\n\
\n\
int main(int argc, char* argv[]) {\n\
	printf(\"Hello, %s\\n\", \"world!\");\n\
	return 0;\n\
}\n\
";
	})
;
