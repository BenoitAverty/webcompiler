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

            spyOn(TemplateService, 'get').and.callFake (code, callback) ->
                callback 'c_basic_content'

            controller = _$controller_ 'CompilerController'

    it 'is not working at the beginning', ->
        expect(controller.working).toBe false

    it 'doesn\'t show any output at the beginning', ->
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
