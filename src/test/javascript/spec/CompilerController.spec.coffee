describe 'CompilerController', ->

    beforeEach module 'webcompiler'

    controller = null
    mockCompilationService = jasmine.createSpyObj 'mockCompilationService',
        ['compile', 'execute', 'watchCompilation', 'watchExecution', 'stopWatch']
    mockTemplateService = jasmine.createSpyObj 'mockTemplateService', ['get']

    beforeEach inject (_$controller_) ->
        mockTemplateService.get.and.callFake (code, callback) ->
            switch code
                when 'c_basic.c' then callback 'c_basic_content'
                when 'c_parameters.c' then callback 'c_parameters_content'
                else callback 'default_mock_content'

        controller = _$controller_ 'CompilerController',
            CompilationService: mockCompilationService
            templateService: mockTemplateService

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
            controller.template = 'c_parameters.c'
            controller.loadTemplate()
            expect(controller.sourcecode).toBe 'c_parameters_content'

    describe 'triggerWorkflow function', ->

        beforeEach inject (_$controller_) ->
            mockCompilationService.compile.and.callFake (source, callback, error) ->
                callback()

            controller = _$controller_ 'CompilerController',
                CompilationService: mockCompilationService
                templateService: mockTemplateService

        it 'sets the compiler to working state', ->
            controller.triggerWorkflow()
            expect(controller.working).toBe true

        it 'empties any output already present', ->
            controller.executionOutput = 'notEmpty'
            controller.compilationOutput = 'notEmpty'
            controller.triggerWorkflow()
            expect(controller.executionOutput).toBe ''
            expect(controller.compilationOutput).toBe ''

        it 'sets the state to "compile" after the compilation service is called', ->
            controller.triggerWorkflow()
            expect(controller.step).toBe 'compile'

        it 'starts to watch the compilation status after the compilation service is called', ->
            controller.triggerWorkflow()
            expect(mockCompilationService.watchCompilation).toHaveBeenCalled()

        it 'Sets the compiler to not working state if an error occurs', ->
            controller.triggerWorkflow()
            expect(controller.working).toBe false
