describe 'CompilerController', ->

    # controller under test
    controller = null

    # services used by the controller (can be spied on)
    CompilationService = null;
    TemplateService = null;

    beforeEach ->
        module 'webcompiler'

    # Retrieve the services to be able to spy on them.
    beforeEach ->
        inject (_$controller_, _TemplateService_, _CompilationService_) ->
            CompilationService = _CompilationService_
            TemplateService = _TemplateService_

            # Spy on the Template Service because it is used at the
            # construction of the controller
            spyOn(TemplateService, 'get').and.callFake (code, callback) ->
                callback 'c_basic_content'

            controller = _$controller_ 'CompilerController'

    describe 'after construction', ->
        it 'is not working', ->
            expect(controller.working).toBe false

        it 'doesn\'t show any output', ->
            expect(controller.compilationOutput).toBe ''
            expect(controller.executionOutput).toBe ''

        it 'starts with default template as source', ->
            expect(controller.template).toBe 'c_basic.c'
            expect(controller.sourcecode).toBe 'c_basic_content'

    describe 'loadTemplate function', ->
        it 'changes the content of the source area when called', ->
            TemplateService.get.and.callFake (code, callback) ->
                if code=='c_parameters.c' then callback 'c_parameters_content'
                else callback 'c_basic_content'

            controller.template = 'c_parameters.c'
            controller.loadTemplate()
            expect(controller.sourcecode).toBe 'c_parameters_content'

    describe 'triggerWorkflow function', ->

        it 'sets the compiler to working state', ->
            controller.triggerWorkflow()
            expect(controller.working).toBe true

        it 'empties any output already present', ->
            controller.executionOutput = 'notEmpty'
            controller.compilationOutput = 'notEmpty'
            controller.triggerWorkflow()
            expect(controller.executionOutput).toBe ''
            expect(controller.compilationOutput).toBe ''

        describe 'when the api accepts the program', ->
            beforeEach ->
                spyOn(CompilationService, 'compile').and.callFake (source, callback, error) ->
                    callback()

            it 'sets the state to "compile"', ->
                controller.triggerWorkflow()
                expect(controller.step).toBe 'compile'

            it 'starts to watch the compilation status', ->
                spyOn CompilationService, 'watchCompilation'
                controller.triggerWorkflow()
                expect(CompilationService.watchCompilation).toHaveBeenCalled()


        describe 'when the api returns an error', ->

            beforeEach ->
                spyOn(CompilationService, 'compile').and.callFake (source, callback, error) ->
                    error()

            it 'Sets the compiler to not working state if an error occurs', ->
                controller.triggerWorkflow()
                expect(controller.working).toBe false

    describe 'manageWorkflow function', ->

        # manageWorkflow is private. We call it as a callback through the "triggerWorkflow" function
        beforeEach ->
            spyOn(CompilationService, 'compile').and.callFake (s, callback, e) ->
                callback()

            spyOn(CompilationService, 'stopWatch')

        afterEach ->
            expect(CompilationService.stopWatch).toHaveBeenCalled()

        describe 'when the status becomes "COMPILED"', ->

            beforeEach ->
                # mock the callback from "watchCompilation"
                spyOn(CompilationService, 'watchCompilation').and.callFake (callback) ->
                    callback
                        status: 'COMPILED'
                        compilationOutput: 'compilation_output'

                spyOn(CompilationService, 'execute')

            it 'displays the compilation output', ->
                controller.triggerWorkflow()
                expect(controller.compilationOutput).toBe 'compilation_output'

            it 'calls the execution method', ->
                controller.triggerWorkflow()
                expect(CompilationService.execute).toHaveBeenCalled()

        describe 'when the status becomes "COMPILATION_ERROR"', ->
            beforeEach ->
                # mock the callback from "watchCompilation"
                spyOn(CompilationService, 'watchCompilation').and.callFake (callback) ->
                    callback
                        status: 'COMPILE_ERROR'
                        compilationOutput: 'compilation_output'

                spyOn(CompilationService, 'execute')

            it 'displays the compilation output', ->
                controller.triggerWorkflow()
                expect(controller.compilationOutput).toBe 'compilation_output'

            it 'doesn\'t call the execution method', ->
                controller.triggerWorkflow()
                expect(CompilationService.execute).not.toHaveBeenCalled()

            it 'clears the working state', ->
                controller.triggerWorkflow()
                expect(controller.step).toBe ''
                expect(controller.working).toBe false

        describe 'when the status indicates the end of the execution', ->
            beforeEach ->
                # mock the callback from "watchCompilation"
                spyOn(CompilationService, 'watchCompilation').and.callFake (callback) ->
                    callback
                        status: 'EXECUTED'
                        executionOutput: 'execution_output'

            it 'diplays the output of the execution', ->
                controller.triggerWorkflow()
                expect(controller.executionOutput).toBe 'execution_output'

            it 'removes blanks and transform carriage returns', ->
                CompilationService.watchCompilation.and.callFake (callback) ->
                    callback
                        status: 'EXECUTED'
                        executionOutput:"""
						Hello,
world!

					"""
                controller.triggerWorkflow()
                expect(controller.executionOutput).toBe 'Hello,<br />world!'

            it 'clears the working state', ->
                controller.triggerWorkflow()
                expect(controller.step).toBe ''
                expect(controller.working).toBe false
