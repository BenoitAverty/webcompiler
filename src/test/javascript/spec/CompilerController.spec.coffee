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
        expect(controller.compilationOutput).toBeEmpty
        expect(controller.executionOutput).toBeEmpty

    it 'starts with default template as source', ->
        expect(controller.template).toBe 'c_basic.c'
        expect(controller.sourcecode).toBe 'c_basic_content'

    describe 'loadTemplate function', ->
        it 'changes the content of the source area when called', ->
            controller.template = 'c_parameters.c'
            controller.loadTemplate
            expect(controller.sourcecode).toBe 'c_parameters_content'
