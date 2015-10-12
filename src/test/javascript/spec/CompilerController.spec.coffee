describe 'CompilerController', ->

    beforeEach module 'webcompiler'

    controller = null;
    beforeEach inject (_$controller_) ->
        controller = _$controller_ 'CompilerController'

    it 'is not working at the beginning', ->
        expect(controller.working).toBe false

    it 'doesn\'t show any output at the beginning', ->
        expect(controller.compilationOutput).toBeEmpty
        expect(controller.executionOutput).toBeEmpty

    it 'starts with default template as source', ->
        expect(controller.sourcecode).toBe("test")

    describe 'loadTemplate function', ->
        it 'changes the content of the source area when called', ->
            expect(true).toBe(false)
