describe 'CompilerController', ->

  beforeEach module 'webcompiler'

  beforeEach inject _$controller_ ->
    $controller = _$controller_

  describe '$controller.loadTemplate', ->
    it 'has a test that always pass', ->
      expect(true).toBe true
