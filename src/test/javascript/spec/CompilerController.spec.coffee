describe 'CompilerController', ->

  beforeEach module 'webcompiler'

  $controller = null;
  beforeEach inject (_$controller_) ->
    $controller = _$controller_

  describe '$controller.loadTemplate', ->
    it 'has a test that always pass', ->
      controller = $controller 'CompilerController'
      expect(controller.working).toBe false
